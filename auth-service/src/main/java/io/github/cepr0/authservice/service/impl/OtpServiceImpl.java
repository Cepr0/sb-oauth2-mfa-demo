package io.github.cepr0.authservice.service.impl;

import io.github.cepr0.authservice.model.Otp;
import io.github.cepr0.authservice.repo.OtpRepo;
import io.github.cepr0.authservice.service.OtpGenerator;
import io.github.cepr0.authservice.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Transactional
public class OtpServiceImpl implements OtpService {

    private final OtpRepo otpRepo;
    private final OtpGenerator otpGenerator;

    public OtpServiceImpl(OtpRepo otpRepo, OtpGenerator otpGenerator) {
        this.otpRepo = otpRepo;
        this.otpGenerator = otpGenerator;
    }

    @Override
    public void createAndSend(String otpToken, String phoneNumber, Duration duration) {
        String otpValue = otpGenerator.generate();
        otpRepo.save(new Otp(otpToken, phoneNumber, otpValue, Instant.now().plus(duration)));
        log.info("[i] OTP sent: '{}'", otpValue);
    }

    @Override
    public Otp findAndRemove(String otpToken) {
        Otp otp = otpRepo.findById(otpToken).orElseThrow(() -> new InvalidGrantException("OTP token not found"));
        otpRepo.deleteById(otpToken);
        if (otp.getExpiredAt().isBefore(Instant.now())) {
            throw new InvalidGrantException("OTP is expired");
        }
        return otp;
    }
}
