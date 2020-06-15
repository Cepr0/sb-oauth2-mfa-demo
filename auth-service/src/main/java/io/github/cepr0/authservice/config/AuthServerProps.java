package io.github.cepr0.authservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Getter
@Setter
@Validated
@ConfigurationProperties("auth")
public class AuthServerProps {

    @Valid
    private Otp otp = new Otp();

    @Valid
    private Jwt jwt = getJwt();

    @NotEmpty private Map<String, Client> clients = Collections.emptyMap();

    @Getter
    @Setter
    public static class Otp {
        private Duration ttl = Duration.ofMinutes(5);
    }

    @Getter
    @Setter
    public static class Jwt {

        /**
         * Key id.
         */
        @NotEmpty private String kid;
        /**
         * The location of the key store.
         */
        @NotNull private Resource keyStore;

        /**
         * The key store's password
         */
        @NotEmpty private String keyStorePassword;

        /**
         * The alias of the key from the key store
         */
        @NotEmpty private String keyAlias;

        /**
         * The password of the key from the key store
         */
        @NotEmpty private String keyPassword;
    }

    @Getter
    @Setter
    public static class Client {
        @NotEmpty private String clientSecret;
        @NotEmpty private String[] scopes;
        @NotEmpty private String[] authorizedGrantTypes;
        @NotNull private Duration accessTokenValidity = Duration.ofHours(1);
        @NotNull private Duration refreshTokenValidity = Duration.ofDays(30);
    }
}
