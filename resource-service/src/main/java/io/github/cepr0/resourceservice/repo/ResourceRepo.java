package io.github.cepr0.resourceservice.repo;

import io.github.cepr0.resourceservice.model.Resource;
import org.springframework.data.keyvalue.repository.KeyValueRepository;

import java.util.List;
import java.util.UUID;

public interface ResourceRepo extends KeyValueRepository<Resource, UUID> {
    List<Resource> findByCustomerId(UUID customerId);
}
