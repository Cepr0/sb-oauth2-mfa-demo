package io.github.cepr0.resourceservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.util.UUID;

@Value
@KeySpace("resources")
public class Resource {
    @Id UUID id;
    @JsonIgnore UUID customerId;
    String name;
}
