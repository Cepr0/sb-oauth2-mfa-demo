package io.github.cepr0.authservice.handler;

import io.github.cepr0.authservice.dto.OtpTokenEvent;
import io.github.cepr0.authservice.model.Otp;
import io.github.cepr0.authservice.repo.OtpRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class OtpSender {

    private final OtpRepo otpRepo;

    public OtpSender(OtpRepo otpRepo) {
        this.otpRepo = otpRepo;
    }

    @Async
    @EventListener
    public void send(OtpTokenEvent event) {
        String tokenId = event.getTokenId();
        int otpValue = ThreadLocalRandom.current().nextInt(100000, 1000000);
        otpRepo.save(new Otp(tokenId, otpValue));
        log.info("[i] OTP sent: '{}'", otpValue);
    }
}
