package io.github.cepr0.resourceservice.controller;

import io.github.cepr0.resourceservice.model.Resource;
import io.github.cepr0.resourceservice.repo.ResourceRepo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceRepo resourceRepo;

    public ResourceController(ResourceRepo resourceRepo) {
        this.resourceRepo = resourceRepo;
    }

    @GetMapping
    public List<Resource> getAll(@AuthenticationPrincipal(expression = "userId") UUID customerId) {
        return resourceRepo.findByCustomerId(customerId);
    }
}
