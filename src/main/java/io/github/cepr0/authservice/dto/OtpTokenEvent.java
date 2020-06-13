package io.github.cepr0.authservice.dto;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class OtpTokenEvent {
    private final String tokenId;
    private final String phoneNumber;

    public OtpTokenEvent(@NonNull String tokenId, String phoneNumber) {
        this.tokenId = tokenId;
        this.phoneNumber = phoneNumber;
    }
}
