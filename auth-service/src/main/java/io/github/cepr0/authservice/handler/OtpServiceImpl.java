package io.github.cepr0.authservice.handler;

import io.github.cepr0.authservice.model.Otp;
import io.github.cepr0.authservice.repo.OtpRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@Transactional
public class OtpServiceImpl implements OtpService {

    private final OtpRepo otpRepo;

    public OtpServiceImpl(OtpRepo otpRepo) {
        this.otpRepo = otpRepo;
    }

    @Override
    public void createAndSend(String otpToken, String phoneNumber, Duration duration) {
        String otpValue = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
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
