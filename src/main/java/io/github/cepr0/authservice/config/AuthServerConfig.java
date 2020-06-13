package io.github.cepr0.authservice.config;

import io.github.cepr0.authservice.dto.CustomUserDetails;
import io.github.cepr0.authservice.grant.OtpGranter;
import io.github.cepr0.authservice.handler.OtpService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    // TODO Move to application props
    // TODO Replace with JWKs
    // https://docs.spring.io/spring-security-oauth2-boot/docs/2.2.x/reference/htmlsingle/#oauth2-boot-authorization-server-spring-security-oauth2-resource-server
    // https://www.baeldung.com/spring-security-oauth2-jws-jwk#5-creating-a-keystore-file
    public static final String TOKEN_KEY = "token-key";

    // TODO Move to application props
    private static final Duration OTP_TTL = Duration.ofMinutes(5);

    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final UserDetailsService userDetailsService;

    public AuthServerConfig(AuthenticationManager authenticationManager, OtpService otpService, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.otpService = otpService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clientDetailsService) throws Exception {
        clientDetailsService.inMemory()
                .withClient("client")
                .secret("{noop}secret")
                .scopes("api")
                .authorizedGrantTypes("otp", "refresh_token")
                .accessTokenValiditySeconds(60 * 60) // 1 our
                .refreshTokenValiditySeconds(60 * 60 * 24 * 30) // 30 days
        ;
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        TokenEnhancerChain chain = new TokenEnhancerChain();
        chain.setTokenEnhancers(List.of(this::enhanceToken, tokenConverter()));
        endpoints.authenticationManager(authenticationManager)
                .reuseRefreshTokens(false)
                .tokenStore(tokenStore())
                .tokenEnhancer(chain)
                .tokenGranter(tokenGranter(endpoints));
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(tokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter tokenConverter() {
        var converter = new JwtAccessTokenConverter();
        converter.setSigningKey(TOKEN_KEY);
        converter.setAccessTokenConverter(new DefaultAccessTokenConverter() {
            @Override
            public OAuth2Authentication extractAuthentication(Map<String, ?> claims) {
                OAuth2Authentication authentication = super.extractAuthentication(claims);
                Authentication userAuth = authentication.getUserAuthentication();
                String phoneNumber = (String) userAuth.getPrincipal();
                String userId = Optional.ofNullable((String) claims.get("user_id"))
                        .orElseThrow(() -> new IllegalStateException("User id doesn't exists in token"));
                var userDetails = new CustomUserDetails(userId, phoneNumber, userAuth.getAuthorities());
                Authentication user = new UsernamePasswordAuthenticationToken(userDetails, "N/A", userAuth.getAuthorities());
                return new OAuth2Authentication(authentication.getOAuth2Request(), user);
            }
        });
        return converter;
    }

    private TokenGranter tokenGranter(AuthorizationServerEndpointsConfigurer endpoints) {
        List<TokenGranter> granters = new ArrayList<>(List.of(endpoints.getTokenGranter()));
        granters.add(new OtpGranter(authenticationManager, endpoints, otpService, OTP_TTL));
        return new CompositeTokenGranter(granters);
    }

    private OAuth2AccessToken enhanceToken(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if (accessToken instanceof DefaultOAuth2AccessToken) {
                String userId = customUserDetails.getUserId();
                ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(Map.of("user_id", userId));
            }
        }
        return accessToken;
    }
}