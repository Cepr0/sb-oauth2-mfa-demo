package io.github.cepr0.authservice.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/resources")
    public Map<String, String> getResources(@AuthenticationPrincipal UserDetails userDetails) {
        return Map.of("userId", userDetails.getUsername());
    }
}
