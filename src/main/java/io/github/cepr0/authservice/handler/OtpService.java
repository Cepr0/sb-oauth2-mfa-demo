package io.github.cepr0.authservice.handler;

import io.github.cepr0.authservice.model.Otp;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;

import java.time.Duration;

public interface OtpService {

    /**
     * Creates and stores {@link Otp} with the given duration.
     * Then sends OTP to the given phone number.
     *
     * @param otpToken must not be null
     * @param phoneNumber must not be null
     * @param duration must not be null
     */
    void createAndSend(String otpToken, String phoneNumber, Duration duration);

    /**
     * Finds {@link Otp} by OTP token and then removes it.
     * Throws {@link InvalidGrantException} when {@link Otp} not found or expired.
     *
     * @param otpToken must not be null
     * @return {@link Otp} if it's not expired
     */
    Otp findAndRemove(String otpToken);
}
