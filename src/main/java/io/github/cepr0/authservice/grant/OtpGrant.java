package io.github.cepr0.authservice.grant;

import io.github.cepr0.authservice.model.Otp;
import io.github.cepr0.authservice.repo.OtpRepo;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.time.Instant;
import java.util.HashMap;
import java.util.Set;

import static java.util.Optional.ofNullable;

public class OtpGrant extends AbstractTokenGranter {

    private static final String GRANT_TYPE = "otp";

    private final AuthenticationManager authenticationManager;
    private final TokenStore tokenStore;
    private final ClientDetailsService clientDetailsService;
    private final OtpRepo otpRepo;

    public OtpGrant(
            AuthenticationManager authenticationManager,
            AuthorizationServerEndpointsConfigurer endpoints,
            OtpRepo otpRepo
    ) {
        super(endpoints.getTokenServices(), endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory(), GRANT_TYPE);
        this.authenticationManager = authenticationManager;
        this.tokenStore = endpoints.getTokenStore();
        this.clientDetailsService = endpoints.getClientDetailsService();
        this.otpRepo = otpRepo;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest otpTokenRequest) {
        var parameters = new HashMap<>(otpTokenRequest.getRequestParameters());

        int otpValue = getOtpValue(parameters);
        String otpTokenValue = getOtpTokenValue(parameters);

        OAuth2AccessToken otpToken = toToken(otpTokenValue);
        String tokenId = (String) otpToken.getAdditionalInformation().get("jti");
        int foundOtp = findOtpValue(tokenId);
        if (foundOtp != otpValue) {
            throw new InvalidGrantException("OTP is mismatched");
        }
        return createAuthentication(otpTokenRequest, toAuthentication(otpToken));
    }

    private OAuth2Authentication createAuthentication(TokenRequest otpTokenRequest, OAuth2Authentication otpAuth) {
        Authentication user = authenticationManager.authenticate(otpAuth.getUserAuthentication());
        var userAuthentication = new OAuth2Authentication(otpAuth.getOAuth2Request(), user);

        OAuth2Request userRequest = userAuthentication.getOAuth2Request();
        String clientId = userRequest.getClientId();
        if (clientId != null && clientId.equals(otpTokenRequest.getClientId())) {
            try {
                ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
                Set<String> clientScopes = client.getScope();
                Set<String> otpRequestScopes = otpTokenRequest.getScope();
                if (!clientScopes.containsAll(otpRequestScopes)) {
                    throw new InvalidScopeException("Requested scope doesn't match the client scopes: ", clientScopes);
                }
                return new OAuth2Authentication(userRequest.narrowScope(otpRequestScopes), userAuthentication.getUserAuthentication());
            } catch (ClientRegistrationException e) {
                throw new InvalidTokenException("Client not valid: " + clientId, e);
            }
        } else {
            throw new InvalidGrantException("Client is missing or doesn't correspond to the OTP token");
        }
    }

    private String getOtpTokenValue(HashMap<String, String> parameters) {
        String value = parameters.get("otp_token");
        if (value == null) {
            throw new InvalidRequestException("Missing OTP token");
        }
        return value;
    }

    private int getOtpValue(HashMap<String, String> parameters) {
        String otpValue = parameters.get("otp");
        if (otpValue == null) throw new InvalidRequestException("Missing OTP");
        try {
            return Integer.parseInt(otpValue);
        } catch (NumberFormatException e) {
            throw new InvalidGrantException("Invalid OTP code");
        }
    }

    private OAuth2AccessToken toToken(String tokenValue) {
        return ofNullable(tokenStore.readAccessToken(tokenValue))
                .map(token -> {
                    if (token.isExpired()) {
                        tokenStore.removeAccessToken(token);
                        throw new InvalidTokenException("Access token expired: " + tokenValue);
                    }
                    return token;
                })
                .orElseThrow(() -> new InvalidTokenException("Invalid access token: " + tokenValue));
    }

    private OAuth2Authentication toAuthentication(OAuth2AccessToken token) {
        return ofNullable(tokenStore.readAuthentication(token))
                .orElseThrow(() -> new InvalidTokenException("Invalid access token: " + token.getValue()));
    }

    private int findOtpValue(String tokenId) {
        Otp otp = otpRepo.findById(tokenId).orElseThrow(() -> new InvalidGrantException("OTP token not found"));
        if (otp.getExpiredAt().isBefore(Instant.now())) {
            throw new InvalidGrantException("OTP is expired");
        }
        return otp.getValue();
    }
}
