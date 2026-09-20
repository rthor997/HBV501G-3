package org.example.klifurapp.DTO.user;

public class LoginResponseDTO {
    private long id;
    private String email;

    public void getId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(long id) {
        this.id = id;
    }
}
