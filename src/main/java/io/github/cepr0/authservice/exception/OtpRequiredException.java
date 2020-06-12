package io.github.cepr0.authservice.exception;

import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

public class OtpRequiredException extends OAuth2Exception {

    public OtpRequiredException(String otpToken) {
        super("OTP is required");
        this.addAdditionalInformation("otp_token", otpToken);
    }

    public String getOAuth2ErrorCode() {
        return "otp_required";
    }

    public int getHttpErrorCode() {
        return 403;
    }
}
