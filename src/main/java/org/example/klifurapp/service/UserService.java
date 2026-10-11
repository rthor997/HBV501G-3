package org.example.klifurapp.service;

import org.example.klifurapp.DTO.user.UpdateUserDTO;
import org.example.klifurapp.entity.User;

public interface UserService {

    void deleteUser(Long id);

    User updateUser(long id, UpdateUserDTO dto);
}