package io.github.cepr0.authservice.controller;

import io.github.cepr0.authservice.dto.DemoResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/resources")
    public Collection<DemoResource> getResources() {
        return List.of(
                new DemoResource(1, "resource #1"),
                new DemoResource(2, "resource #2")
        );
    }
}
