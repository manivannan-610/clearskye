package com.clearskye.epicconnector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class EpicApiConnectorApplication {
	public static void main(String[] args) {
		SpringApplication.run(EpicApiConnectorApplication.class, args);
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
