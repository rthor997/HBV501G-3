package org.example.klifurapp.DTO.requests;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateUserRequestDTO {

    @Pattern(regexp = ".*\\S.*", message = "Username must not be blank")
    @Size(max = 50, message = "Username must be at most 50 characters")
    private String username;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
