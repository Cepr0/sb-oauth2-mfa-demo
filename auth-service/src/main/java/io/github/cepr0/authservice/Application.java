package io.github.cepr0.authservice;

import io.github.cepr0.authservice.model.Customer;
import io.github.cepr0.authservice.repo.CustomerRepo;
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

    private final CustomerRepo customerRepo;

    public Application(CustomerRepo customerRepo) {
        this.customerRepo = customerRepo;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        customerRepo.saveAll(List.of(
                new Customer(
                        UUID.fromString("fcc8cece-d464-4b7b-8925-b13f0461f263"),
                        "123456789",
                        "John Smith"
                ),
                new Customer(
                        UUID.fromString("61a9971a-ef28-4b65-8308-72b475a68f63"),
                        "987654321",
                        "Joan Doe"
                )
        ));
    }
}
