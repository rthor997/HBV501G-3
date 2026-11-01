package org.example.klifurapp.DTO.requests;

public record AdminUpdateUserDTO (
    String username,
    String email,
    String password
){}