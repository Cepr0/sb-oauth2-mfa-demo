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
import java.util.Map;

@Getter
@Setter
@Validated
@ConfigurationProperties("auth")
public class AuthServerProps {

    @Valid
    @NotNull
    private Otp otp;

    @Valid
    @NotNull
    private Jwt jwt;

    @NotEmpty
    private Map<String, Client> clients;

    @Getter
    @Setter
    public static class Otp {
        /**
         * Time to live of OTP.
         */
        private Duration ttl = Duration.ofMinutes(5);

        /**
         * OTP type, defaults to constant.
         */
        private OtpType type = OtpType.constant;
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
        /**
         * Client secret.
         */
        @NotEmpty private String clientSecret;

        /**
         * Collection of the client scopes.
         */
        @NotEmpty private String[] scopes;

        /**
         * Collection of the client authorized grants.
         */
        @NotEmpty private String[] authorizedGrantTypes;

        /**
         * Access token validity, defaults to 1 hour.
         */
        @NotNull private Duration accessTokenValidity = Duration.ofHours(1);

        /**
         * Refresh token validity, defaults to 30 days.
         */
        @NotNull private Duration refreshTokenValidity = Duration.ofDays(30);
    }

    public enum OtpType {
        constant, random
    }
}
