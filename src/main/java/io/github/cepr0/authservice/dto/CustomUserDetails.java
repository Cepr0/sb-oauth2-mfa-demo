package io.github.cepr0.authservice.dto;

import io.github.cepr0.authservice.model.Customer;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

import static io.github.cepr0.authservice.grant.OtpGranter.NA_PASSWORD;
import static java.util.stream.Collectors.toList;

@Getter
public class CustomUserDetails extends User {
    private final String userId;

    public CustomUserDetails(Customer customer) {
        super(
                customer.getPhoneNumber(),
                "{noop}" + NA_PASSWORD,
                customer.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(toList())
        );
        this.userId = customer.getId().toString();
    }

    public CustomUserDetails(String userId, String phoneNumber, Collection<? extends GrantedAuthority> authorities) {
        super(phoneNumber, "{noop}" + NA_PASSWORD, authorities);
        this.userId = userId;
    }
}
