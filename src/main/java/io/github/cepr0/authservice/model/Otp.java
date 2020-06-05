package io.github.cepr0.authservice.model;

import lombok.Value;
import lombok.experimental.Tolerate;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.MINUTES;

@Value
@KeySpace("otp")
public class Otp {
    @Id String tokenId;
    int otp;
    Instant expiredAt;

    @Tolerate
    public Otp(String tokenId, int otp) {
        this.tokenId = tokenId;
        this.otp = otp;
        expiredAt = Instant.now().plus(5, MINUTES);
    }
}
