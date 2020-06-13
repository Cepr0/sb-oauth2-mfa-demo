package io.github.cepr0.authservice.handler;

import io.github.cepr0.authservice.dto.OtpTokenEvent;
import io.github.cepr0.authservice.model.Otp;
import io.github.cepr0.authservice.repo.OtpRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class OtpService {

    // TODO Move to application props
    private static final Duration OTP_TTL = Duration.ofMinutes(5);

    private final OtpRepo otpRepo;

    public OtpService(OtpRepo otpRepo) {
        this.otpRepo = otpRepo;
    }

    @Async
    @EventListener
    public void createAndSend(OtpTokenEvent event) {
        String tokenId = event.getTokenId();
        String phoneNumber = event.getPhoneNumber();
        String otpValue = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        otpRepo.save(new Otp(tokenId, phoneNumber, otpValue, Instant.now().plus(OTP_TTL)));
        log.info("[i] OTP sent: '{}'", otpValue);
    }
}
