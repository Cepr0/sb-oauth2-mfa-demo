package io.github.cepr0.authservice.grant;

import io.github.cepr0.authservice.dto.OtpTokenEvent;
import io.github.cepr0.authservice.exception.OtpRequiredException;
import io.github.cepr0.authservice.model.Otp;
import io.github.cepr0.authservice.repo.OtpRepo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

public class OtpGranter extends AbstractTokenGranter {

    public static final String NA_PASSWORD = "N/A";
    private static final String GRANT_TYPE = "otp";

    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;
    private final OtpRepo otpRepo;

    public OtpGranter(
            @NonNull AuthenticationManager authenticationManager,
            @NonNull AuthorizationServerEndpointsConfigurer endpoints,
            @NonNull ApplicationEventPublisher eventPublisher,
            @NonNull OtpRepo otpRepo
    ) {
        super(endpoints.getTokenServices(), endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory(), GRANT_TYPE);
        this.authenticationManager = authenticationManager;
        this.eventPublisher = eventPublisher;
        this.otpRepo = otpRepo;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        var parameters = new HashMap<>(tokenRequest.getRequestParameters());
        String phoneNumber = parameters.get("phone_number");
        String otpValue = parameters.get("otp");
        String otpToken = parameters.get("otp_token");

        if (phoneNumber == null && otpValue == null && otpToken == null) {
            throw new InvalidRequestException("Missing Phone number, or OTP value and OTP token");
        }

        // Pre-authenticate with phone number
        if (phoneNumber != null) {
            authenticate(phoneNumber);
            String newOtpToken = UUID.randomUUID().toString();
            eventPublisher.publishEvent(new OtpTokenEvent(newOtpToken, phoneNumber));
            throw new OtpRequiredException(newOtpToken);
        }

        // Authenticate with OTP
        if (otpValue == null) throw new InvalidRequestException("Missing OTP value");
        if (otpToken == null) throw new InvalidRequestException("Missing OTP token");

        Otp otp = findOtp(otpToken);
        if (!otpValue.equals(otp.getValue())) {
            throw new InvalidGrantException("OTP is mismatched");
        }

        var result = authenticate(otp.getPhoneNumber());
        var oAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(oAuth2Request, result);
    }

    private Authentication authenticate(String phoneNumber) {
        Authentication auth = new UsernamePasswordAuthenticationToken(phoneNumber, NA_PASSWORD);
        try {
            Authentication result = authenticationManager.authenticate(auth);
            if (result == null || !result.isAuthenticated()) {
                throw new InvalidGrantException("Could not authenticate user by phone number");
            }
            return result;
        } catch (AuthenticationException e) {
            throw new InvalidGrantException(e.getMessage());
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
