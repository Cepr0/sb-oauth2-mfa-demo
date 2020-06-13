package io.github.cepr0.authservice.model;

import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.util.Set;
import java.util.UUID;

@Value
@KeySpace("customers")
public class Customer {
    @Id UUID id;
    String phoneNumber;
    String name;
    Set<String> roles = Set.of("USER");
}
