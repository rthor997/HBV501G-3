package org.example.klifurapp.DTO.user;

public record AdminUpdateUserDTO (
    String username,
    String email,
    String password
){}