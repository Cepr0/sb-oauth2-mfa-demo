package io.github.cepr0.authservice.repo;

import io.github.cepr0.authservice.model.Otp;
import org.springframework.data.repository.CrudRepository;

public interface OtpRepo extends CrudRepository<Otp, String> {
}
