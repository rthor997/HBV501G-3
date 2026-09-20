package org.example.klifurapp.service;

import org.example.klifurapp.DTO.user.CreateUserDTO;
import org.example.klifurapp.DTO.user.LoginRequestDTO;
import org.example.klifurapp.DTO.user.LoginResponseDTO;
import org.example.klifurapp.entity.User;

public interface AuthService {
    User register(CreateUserDTO dto);
    LoginResponseDTO authenticate(LoginRequestDTO dto);
}
