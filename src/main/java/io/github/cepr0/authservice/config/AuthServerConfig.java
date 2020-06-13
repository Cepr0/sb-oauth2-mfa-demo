package io.github.cepr0.authservice.config;

import io.github.cepr0.authservice.grant.OtpGranter;
import io.github.cepr0.authservice.repo.OtpRepo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    // TODO Move to application props
    // TODO Replace with JWKs
    // https://docs.spring.io/spring-security-oauth2-boot/docs/2.2.x/reference/htmlsingle/#oauth2-boot-authorization-server-spring-security-oauth2-resource-server
    // https://www.baeldung.com/spring-security-oauth2-jws-jwk#5-creating-a-keystore-file
    public static final String TOKEN_KEY = "token-key";

    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;
    private final OtpRepo otpRepo;

    public AuthServerConfig(AuthenticationManager authenticationManager, ApplicationEventPublisher eventPublisher, OtpRepo otpRepo) {
        this.authenticationManager = authenticationManager;
        this.eventPublisher = eventPublisher;
        this.otpRepo = otpRepo;
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
        endpoints
                .tokenStore(tokenStore())
                .reuseRefreshTokens(false)
                .tokenEnhancer(tokenConverter())
                .authenticationManager(authenticationManager)
                .tokenGranter(tokenGranter(endpoints));
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(tokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter tokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey(TOKEN_KEY);
        return converter;
    }

    private TokenGranter tokenGranter(AuthorizationServerEndpointsConfigurer endpoints) {
        List<TokenGranter> granters = new ArrayList<>(List.of(endpoints.getTokenGranter()));
        granters.add(new OtpGranter(authenticationManager, endpoints, eventPublisher, otpRepo));
        return new CompositeTokenGranter(granters);
    }
}