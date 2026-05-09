package com.grocery.service_user.service;

import com.grocery.service_user.DTO.UserDTO;
import com.grocery.service_user.entity.User;
import com.grocery.service_user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    UserRepository userRepository;
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }



    @Override
    public List<User> getUsers() {
        log.debug("Fetching all users");
        List<User> users = userRepository.findAll();
        log.debug("Users fetched | count={}", users.size());
        return users;
    }

    @Override
    public User getUser(long id) {
        log.debug("Fetching user | id={}", id);
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            log.debug("User found | id={} | email={}", id, user.getEmail());
        } else {
            log.warn("User not found | id={}", id);
        }
        return user;
    }

    @Override
    public User updateUser(UserDTO product,String email) {
        log.info("Updating user | email={}", email);
        try {
            User user = userRepository.findByEmail(email);
            if (user != null) {
                user.setUsername(product.getUsername());
                user.setEmail(product.getEmail());
                user.setPhoneNumber(product.getPhoneNumber());
                user.setAddress(product.getAddress());
                User saved = userRepository.save(user);
                log.info("User updated successfully | email={} | newEmail={}", email, product.getEmail());
                return saved;
            }
            log.warn("User not found for update | email={}", email);
            return null;
        } catch (Exception e) {
            log.error("Failed to update user | email={} | error={}", email, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteUser(String email) {
        log.info("Deleting user | email={}", email);
        try {
            userRepository.deleteByEmail(email);
            log.info("User deleted successfully | email={}", email);
        } catch (Exception e) {
            log.error("Failed to delete user | email={} | error={}", email, e.getMessage(), e);
            throw e;
        }
    }



}
