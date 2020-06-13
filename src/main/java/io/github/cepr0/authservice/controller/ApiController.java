package io.github.cepr0.authservice.controller;

import io.github.cepr0.authservice.dto.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/resources")
    public Map<String, String> getResources(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return Map.of("userId", userDetails.getUserId());
    }
}
