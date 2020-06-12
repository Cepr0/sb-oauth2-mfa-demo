package io.github.cepr0.authservice.model;

import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.time.Instant;

@Value
@KeySpace("otp")
public class Otp {
    @Id String tokenId;
    String phoneNumber;
    int value;
    Instant expiredAt;
}
