package io.github.cepr0.authservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@EnableResourceServer
@Configuration
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/otp/**").access("#oauth2.hasScope('otp')")
                .antMatchers("/api/**").access("#oauth2.hasScope('regular')")
                .antMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated();
    }
}
