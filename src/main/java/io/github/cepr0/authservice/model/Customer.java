package io.github.cepr0.authservice.model;

import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

@Value
@KeySpace("customers")
public class Customer {
    @Id String phoneNumber;
    String name;
}
