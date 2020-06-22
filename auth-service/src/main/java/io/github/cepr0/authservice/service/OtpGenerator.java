package io.github.cepr0.authservice.service;

public interface OtpGenerator {
    /**
     * @return Generated OTP
     */
    String generate();
}
