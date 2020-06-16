package io.github.cepr0.resourceservice;

import io.github.cepr0.resourceservice.model.Resource;
import io.github.cepr0.resourceservice.repo.ResourceRepo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.map.repository.config.EnableMapRepositories;

import java.util.List;
import java.util.UUID;

@EnableMapRepositories
@SpringBootApplication
public class Application {

    private final ResourceRepo resourceRepo;

    public Application(ResourceRepo resourceRepo) {
        this.resourceRepo = resourceRepo;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        resourceRepo.saveAll(List.of(
                new Resource(
                        UUID.randomUUID(),
                        UUID.fromString("fcc8cece-d464-4b7b-8925-b13f0461f263"),
                        "Resource1"
                ),
                new Resource(
                        UUID.randomUUID(),
                        UUID.fromString("fcc8cece-d464-4b7b-8925-b13f0461f263"),
                        "Resource2"
                ),
                new Resource(
                        UUID.randomUUID(),
                        UUID.fromString("61a9971a-ef28-4b65-8308-72b475a68f63"),
                        "Resource3"
                ),
                new Resource(
                        UUID.randomUUID(),
                        UUID.fromString("61a9971a-ef28-4b65-8308-72b475a68f63"),
                        "Resource4"
                )
        ));
    }
}
