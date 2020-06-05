package io.github.cepr0.authservice.dto;

import lombok.Value;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Value
public class OtpDto {
    @Min(100000) @Max(999999) int otp;
}
