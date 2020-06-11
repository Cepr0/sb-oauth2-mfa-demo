package io.github.cepr0.authservice.dto;

import lombok.Getter;
import lombok.NonNull;

public class OtpTokenEvent {
    @Getter private final String tokenId;

    public OtpTokenEvent(@NonNull String tokenId) {
        this.tokenId = tokenId;
    }
}
