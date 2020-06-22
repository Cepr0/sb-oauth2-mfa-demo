package io.github.cepr0.authservice.service.impl;

import io.github.cepr0.authservice.service.OtpGenerator;

public class ConstantOtpGenerator implements OtpGenerator {
    @Override
    public String generate() {
        return "123456";
    }
}
