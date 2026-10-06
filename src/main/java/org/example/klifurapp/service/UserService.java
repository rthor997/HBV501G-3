package org.example.klifurapp.service;

import org.example.klifurapp.DTO.user.UserDTO;

public interface UserService {

    UserDTO getUser(Long id);
    void deleteUser(Long id);
}