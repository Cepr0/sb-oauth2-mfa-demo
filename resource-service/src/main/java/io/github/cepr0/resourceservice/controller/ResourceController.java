package io.github.cepr0.resourceservice.controller;

import io.github.cepr0.resourceservice.repo.ResourceRepo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceRepo resourceRepo;

    public ResourceController(ResourceRepo resourceRepo) {
        this.resourceRepo = resourceRepo;
    }

    @GetMapping
    public Object getAll(@AuthenticationPrincipal Jwt jwt) {
//        return resourceRepo.findByCustomerId(customerId);
        return Map.of("key", "value");
    }
}
