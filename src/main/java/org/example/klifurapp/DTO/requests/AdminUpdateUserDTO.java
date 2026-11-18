package org.example.klifurapp.DTO.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminUpdateUserDTO (

    @Pattern(regexp = ".*\\S.*", message = "Username must not be blank")
    @Size(max = 50, message = "Username must be at most 50 characters")
    String username,

    @Pattern(regexp = ".*\\S.*", message = "Email must not be blank")
    @Email(message = "Email must be valid")
    String email,

    @Size(min = 8, message = "Password must be at least 8 characters")
    String password
){}