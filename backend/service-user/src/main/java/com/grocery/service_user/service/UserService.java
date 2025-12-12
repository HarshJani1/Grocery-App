package com.grocery.service_user.service;

import com.grocery.service_user.DTO.UserDTO;
import com.grocery.service_user.entity.User;

import java.util.List;

public interface UserService {
//    User addUser(User user);
    List<User> getUsers();
    User getUser(long id);
    User updateUser(UserDTO product, String email);
    void deleteUser(String email);
}
