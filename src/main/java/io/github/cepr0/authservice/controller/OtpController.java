package io.github.cepr0.authservice.controller;

import io.github.cepr0.authservice.OAuth2AccessTokenGenerator;
import io.github.cepr0.authservice.dto.OtpDto;
import io.github.cepr0.authservice.model.Otp;
import io.github.cepr0.authservice.repo.OtpRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@RestController
@RequestMapping("/otp")
public class OtpController {

    private final OtpRepo otpRepo;
    private final OAuth2AccessTokenGenerator tokenGenerator;

    public OtpController(OtpRepo otpRepo, OAuth2AccessTokenGenerator tokenGenerator) {
        this.otpRepo = otpRepo;
        this.tokenGenerator = tokenGenerator;
    }

    @PutMapping("/me/send")
    public OtpDto sendOtp(OAuth2Authentication auth) {
        Map<?, ?> decodedDetails = (Map<?, ?>) ((OAuth2AuthenticationDetails) auth.getDetails()).getDecodedDetails();
        String tokenId = (String) decodedDetails.get("tokenId");

        int otpValue = ThreadLocalRandom.current().nextInt(100000, 1000000);

        Otp otp = new Otp(tokenId, otpValue);
        otpRepo.save(otp);

        log.info("[i] OTP sent: '{}'", otpValue);
        return new OtpDto(otp.getOtp());
    }

    @PostMapping
    public OAuth2AccessToken checkOtp(OAuth2Authentication auth, @RequestBody @Valid OtpDto request) {
        int otpValue = request.getOtp();

        Map<?, ?> decodedDetails = (Map<?, ?>) ((OAuth2AuthenticationDetails) auth.getDetails()).getDecodedDetails();
        String tokenId = (String) decodedDetails.get("tokenId");

        int foundOtp = otpRepo.findById(tokenId)
                .filter(otp -> otp.getOtp() == otpValue)
                .filter(otp -> otp.getExpiredAt().isAfter(Instant.now()))
                .map(Otp::getOtp)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "OTP not found or expired"));

        log.info("[i] OTP found: '{}'", foundOtp);
        User user = new User((String) auth.getPrincipal(), "*", auth.getAuthorities());
        return tokenGenerator.generate(user, "regular", Set.of("regular"));
    }
}
