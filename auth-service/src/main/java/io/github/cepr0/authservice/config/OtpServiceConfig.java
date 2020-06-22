package io.github.cepr0.authservice.config;

import io.github.cepr0.authservice.repo.OtpRepo;
import io.github.cepr0.authservice.service.OtpGenerator;
import io.github.cepr0.authservice.service.OtpService;
import io.github.cepr0.authservice.service.impl.ConstantOtpGenerator;
import io.github.cepr0.authservice.service.impl.OtpServiceImpl;
import io.github.cepr0.authservice.service.impl.RandomOtpGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OtpServiceConfig {

    @Bean
    public OtpService otpService(OtpRepo otpRepo, AuthServerProps props) {
        AuthServerProps.OtpType otpType = props.getOtp().getType();
        OtpGenerator otpGenerator;
        if (otpType == AuthServerProps.OtpType.random) {
            otpGenerator = new RandomOtpGenerator();
        } else {
            otpGenerator = new ConstantOtpGenerator();
        }
        return new OtpServiceImpl(otpRepo, otpGenerator);
    }
}
