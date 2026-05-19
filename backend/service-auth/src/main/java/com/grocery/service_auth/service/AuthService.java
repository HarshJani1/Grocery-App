package com.grocery.service_auth.service;

import com.grocery.service_auth.config.RabbitMQConfig;
import com.grocery.service_auth.dto.WelcomeEvent;
import com.grocery.service_auth.entity.User;
import com.grocery.service_auth.repository.UserCredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserCredentialRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RabbitTemplate rabbitTemplate;

    public AuthService(UserCredentialRepository repository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Save a new user and evict the cache for that email in case it was cached before.
     */
    @CacheEvict(value = "users", key = "#credential.email")
    public String saveUser(User credential) {
        log.info("Saving user | email={}", credential.getEmail());
        try {
            credential.setPassword(passwordEncoder.encode(credential.getPassword()));
            repository.save(credential);
            log.info("User saved successfully | email={}", credential.getEmail());

            // Publish welcome email event to RabbitMQ (async — does not block signup response)
            WelcomeEvent event = new WelcomeEvent(credential.getEmail(), credential.getUsername());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.WELCOME_ROUTING_KEY, event);
            log.info("WelcomeEvent published | email={}", credential.getEmail());

            return "user added to the system";
        } catch (Exception e) {
            log.error("Failed to save user | email={} | error={}", credential.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Cache user lookups by email — avoids repeated DB hits on every auth request.
     * Returns User directly (nullable). Optional was causing Jackson to unwrap the
     * inner value during deserialization, breaking SpEL's isPresent() evaluation.
     * TTL: 10 minutes (configured in RedisConfig).
     */
    @Cacheable(value = "users", key = "#email", unless = "#result == null")
    public User findUserByEmail(String email) {
        log.debug("Finding user by email | email={} | cache=MISS", email);
        User user = repository.findByEmail(email).orElse(null);
        if (user != null) {
            log.debug("User found | email={}", email);
        } else {
            log.warn("User not found | email={}", email);
        }
        return user;
    }

    public String generateToken(String email) {
        log.info("Generating JWT token | email={}", email);
        try {
            String token = jwtService.generateToken(email);
            log.info("JWT token generated successfully | email={}", email);
            return token;
        } catch (Exception e) {
            log.error("JWT token generation failed | email={} | error={}", email, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Cache token validation results — avoids re-parsing and re-verifying JWT
     * signature on every downstream request. TTL matches JWT expiry (30 min).
     */
    @Cacheable(value = "tokens", key = "#token")
    public void validateToken(String token) {
        log.debug("Validating JWT token | cache=MISS");
        try {
            jwtService.validateToken(token);
            log.debug("JWT token is valid");
        } catch (Exception e) {
            log.error("JWT token validation failed | error={}", e.getMessage(), e);
            throw e;
        }
    }

}