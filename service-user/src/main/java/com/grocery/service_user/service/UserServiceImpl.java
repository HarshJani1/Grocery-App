package com.grocery.service_user.service;

import com.grocery.service_user.entity.User;
import com.grocery.service_user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {



    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    UserRepository userRepository;
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUser(long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User updateUser(User product) {
        User user = getUser(product.getId());
        if (user != null) {
            user.setUsername(product.getUsername());
            user.setPassword(product.getPassword());
            user.setEmail(product.getEmail());
            user.setPhone(product.getPhone());
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }



}
