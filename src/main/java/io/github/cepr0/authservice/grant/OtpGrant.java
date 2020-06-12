package io.github.cepr0.authservice.grant;

import io.github.cepr0.authservice.model.Otp;
import io.github.cepr0.authservice.repo.OtpRepo;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;

import java.time.Instant;
import java.util.HashMap;

import static io.github.cepr0.authservice.grant.PhoneGrant.NA_PASSWORD;

public class OtpGrant extends AbstractTokenGranter {

    private static final String GRANT_TYPE = "otp";

    private final AuthenticationManager authenticationManager;
    private final OtpRepo otpRepo;

    public OtpGrant(
            AuthenticationManager authenticationManager,
            AuthorizationServerEndpointsConfigurer endpoints,
            OtpRepo otpRepo
    ) {
        super(endpoints.getTokenServices(), endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory(), GRANT_TYPE);
        this.authenticationManager = authenticationManager;
        this.otpRepo = otpRepo;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest otpTokenRequest) {
        var parameters = new HashMap<>(otpTokenRequest.getRequestParameters());
        int otpValue = getOtpValue(parameters);
        String tokenId = getOtpToken(parameters);
        Otp otp = findOtp(tokenId);
        if (otp.getValue() != otpValue) {
            throw new InvalidGrantException("OTP is mismatched");
        }
        String phoneNumber = otp.getPhoneNumber();
        Authentication auth = new UsernamePasswordAuthenticationToken(phoneNumber, NA_PASSWORD);
        Authentication user = authenticationManager.authenticate(auth);
        var oAuth2Request = getRequestFactory().createOAuth2Request(client, otpTokenRequest);
        return new OAuth2Authentication(oAuth2Request, user);
    }

    private String getOtpToken(HashMap<String, String> parameters) {
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

    private Otp findOtp(String tokenId) {
        Otp otp = otpRepo.findById(tokenId).orElseThrow(() -> new InvalidGrantException("OTP token not found"));
        if (otp.getExpiredAt().isBefore(Instant.now())) {
            throw new InvalidGrantException("OTP is expired");
        }
        return otp;
    }
}
