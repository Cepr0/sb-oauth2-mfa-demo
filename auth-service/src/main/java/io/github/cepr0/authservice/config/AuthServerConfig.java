package io.github.cepr0.authservice.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import io.github.cepr0.authservice.dto.CustomUserDetails;
import io.github.cepr0.authservice.grant.OtpGranter;
import io.github.cepr0.authservice.handler.OtpService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
@EnableConfigurationProperties(AuthServerProps.class)
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    private final AuthServerProps props;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    public AuthServerConfig(AuthServerProps props, AuthenticationManager authenticationManager, OtpService otpService) {
        this.props = props;
        this.authenticationManager = authenticationManager;
        this.otpService = otpService;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clientDetailsService) throws Exception {
        var builder = clientDetailsService.inMemory();
        props.getClients().forEach((clientId, client) -> builder.withClient(clientId)
                .secret("{noop}" + client.getClientSecret())
                .scopes(client.getScopes())
                .authorizedGrantTypes(client.getAuthorizedGrantTypes())
                .accessTokenValiditySeconds((int) client.getAccessTokenValidity().toSeconds())
                .refreshTokenValiditySeconds((int) client.getRefreshTokenValidity().toSeconds()));
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
        Map<String, String> customHeaders = Map.of("kid", props.getJwt().getKid());
        var converter = new JwtCustomHeadersAccessTokenConverter(customHeaders, keyPair());
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

    @Bean
    public KeyPair keyPair() {
        Resource ksFile = props.getJwt().getKeyStore();
        KeyStoreKeyFactory ksFactory = new KeyStoreKeyFactory(ksFile, props.getJwt().getKeyStorePassword().toCharArray());
        return ksFactory.getKeyPair(props.getJwt().getKeyAlias());
    }

    @Bean
    public JWKSet jwkSet() {
        RSAKey.Builder builder = new RSAKey.Builder((RSAPublicKey) keyPair().getPublic()).keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID(props.getJwt().getKid());
        return new JWKSet(builder.build());
    }

    private TokenGranter tokenGranter(AuthorizationServerEndpointsConfigurer endpoints) {
        List<TokenGranter> granters = new ArrayList<>(List.of(endpoints.getTokenGranter()));
        granters.add(new OtpGranter(authenticationManager, endpoints, otpService, props.getOtp().getTtl()));
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