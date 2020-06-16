package io.github.cepr0.authservice.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@FrameworkEndpoint
public class JwkSetEndpoint {

    private final JWKSet jwkSet;

    public JwkSetEndpoint(KeyPair keyPair, AuthServerProps props) {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID(props.getJwt().getKid())
                .build();
        jwkSet = new JWKSet(rsaKey);
    }

    @ResponseBody
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> keys() {
        return jwkSet.toJSONObject();
    }
}
