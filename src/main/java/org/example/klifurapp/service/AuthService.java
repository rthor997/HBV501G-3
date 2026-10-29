package org.example.klifurapp.service;

import org.example.klifurapp.DTO.requests.CreateUserRequestDTO;
import org.example.klifurapp.DTO.requests.LoginRequestDTO;
import org.example.klifurapp.DTO.responses.LoginResponseDTO;
import org.example.klifurapp.entity.User;

public interface AuthService {
    User register(CreateUserRequestDTO dto);
    LoginResponseDTO login(LoginRequestDTO dto);
}
