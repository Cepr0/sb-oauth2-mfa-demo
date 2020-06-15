package io.github.cepr0.authservice.repo;

import io.github.cepr0.authservice.model.Customer;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CustomerRepo extends CrudRepository<Customer, String> {
    Optional<Customer> getByPhoneNumber(String phoneNumber);
}
