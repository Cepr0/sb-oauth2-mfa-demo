package io.github.cepr0.authservice.dto;

import io.github.cepr0.authservice.model.Customer;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import static io.github.cepr0.authservice.grant.OtpGranter.NA_PASSWORD;
import static java.util.stream.Collectors.toList;

@Getter
public class CustomUserDetails extends User {
    private final String userId;

    public CustomUserDetails(Customer customer) {
        super(customer.getPhoneNumber(), "{noop}" + NA_PASSWORD, customer.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(toList()));
        this.userId = customer.getId().toString();
    }
}
