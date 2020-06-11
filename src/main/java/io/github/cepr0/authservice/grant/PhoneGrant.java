package io.github.cepr0.authservice.grant;

import io.github.cepr0.authservice.dto.OtpTokenEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.HashMap;
import java.util.Map;

public class PhoneGrant extends AbstractTokenGranter {

    private static final String GRANT_TYPE = "phone";

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

        Authentication auth = new UsernamePasswordAuthenticationToken(phoneNumber, "N/A");
        ((AbstractAuthenticationToken) auth).setDetails(parameters);

        try {
            auth = authenticationManager.authenticate(auth);
        } catch (AccountStatusException | BadCredentialsException ase) {
            throw new InvalidGrantException(ase.getMessage());
        }

        if (auth == null || !auth.isAuthenticated()) {
            throw new InvalidGrantException("Could not authenticate user: " + phoneNumber);
        }

        var oAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
        var token = new UsernamePasswordAuthenticationToken(phoneNumber, "N/A", auth.getAuthorities());
        var authentication = new OAuth2Authentication(oAuth2Request, token);
        var otpToken = getTokenServices().createAccessToken(authentication);
        eventPublisher.publishEvent(new OtpTokenEvent((String) otpToken.getAdditionalInformation().get("jti")));
        throw new OtpRequiredException(otpToken.getValue());
    }
}
