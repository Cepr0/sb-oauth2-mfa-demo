package io.github.cepr0.authservice.grant;

import io.github.cepr0.authservice.dto.OtpTokenEvent;
import io.github.cepr0.authservice.exception.OtpRequiredException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PhoneGrant extends AbstractTokenGranter {

    private static final String GRANT_TYPE = "phone";
    public static final String NA_PASSWORD = "N/A";

    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;

    public PhoneGrant(
            AuthenticationManager authenticationManager,
            AuthorizationServerTokenServices tokenServices,
            ClientDetailsService clientDetailsService,
            OAuth2RequestFactory requestFactory,
            ApplicationEventPublisher eventPublisher
    ) {
        super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
        this.authenticationManager = authenticationManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = new HashMap<>(tokenRequest.getRequestParameters());
        String phoneNumber = parameters.get("phone_number");

        Authentication auth = new UsernamePasswordAuthenticationToken(phoneNumber, NA_PASSWORD);
        ((AbstractAuthenticationToken) auth).setDetails(parameters);

        try {
            auth = authenticationManager.authenticate(auth);
        } catch (AccountStatusException | BadCredentialsException ase) {
            throw new InvalidGrantException(ase.getMessage());
        }

        if (auth == null || !auth.isAuthenticated()) {
            throw new InvalidGrantException("Could not authenticate user: " + phoneNumber);
        }

        String otpToken = UUID.randomUUID().toString();
        eventPublisher.publishEvent(new OtpTokenEvent(otpToken, phoneNumber));
        throw new OtpRequiredException(otpToken);
    }
}
