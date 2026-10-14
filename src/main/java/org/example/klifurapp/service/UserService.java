package org.example.klifurapp.service;

<<<<<<< HEAD
import org.example.klifurapp.DTO.user.UserDTO;
=======
import org.example.klifurapp.DTO.user.UpdateUserDTO;
import org.example.klifurapp.entity.User;
>>>>>>> origin/main

public interface UserService {

    UserDTO getUser(Long id);
    void deleteUser(Long id);

    User updateUser(long id, UpdateUserDTO dto);
}