package io.github.cepr0.authservice.service.impl;

import io.github.cepr0.authservice.service.OtpGenerator;

import java.util.concurrent.ThreadLocalRandom;

public class RandomOtpGenerator implements OtpGenerator {
    @Override
    public String generate() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }
}
