package io.github.cepr0.authservice.config;

import io.github.cepr0.authservice.grant.PhoneGrant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    public static final String TOKEN_KEY = "token-key";

    private final AuthenticationManager authenticationManager;

    public AuthServerConfig(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clientDetailsService) throws Exception {
        clientDetailsService.inMemory()
                .withClient("otp")
                .secret("{noop}otp")
                .scopes("otp")
                .authorizedGrantTypes("phone")
                .accessTokenValiditySeconds(60 * 5) // 5 min
                .and()
                .withClient("regular")
                .secret("{noop}regular")
                .scopes("regular")
                .authorizedGrantTypes("refresh_token")
                .accessTokenValiditySeconds(60 * 60) // 1 our
                .refreshTokenValiditySeconds(60 * 60 * 24 * 30); // 30 days
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
        converter.setAccessTokenConverter(new DefaultAccessTokenConverter() {
            @Override
            public OAuth2Authentication extractAuthentication(Map<String, ?> claims) {
                OAuth2Authentication authentication = super.extractAuthentication(claims);
                String tokenId = (String) Optional.ofNullable(claims.get("jti"))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token doesn't contain 'jti' value"));
                authentication.setDetails(Map.of("tokenId", tokenId));
                return authentication;
            }
        });
        return converter;
    }

    private TokenGranter tokenGranter(AuthorizationServerEndpointsConfigurer endpoints) {
        List<TokenGranter> granters = new ArrayList<>(List.of(endpoints.getTokenGranter()));
        granters.add(new PhoneGrant(
                authenticationManager,
                endpoints.getTokenServices(),
                endpoints.getClientDetailsService(),
                endpoints.getOAuth2RequestFactory()
        ));
        return new CompositeTokenGranter(granters);
    }
}