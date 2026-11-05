package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.CreateUserRequestDTO;
import org.example.klifurapp.DTO.requests.LoginRequestDTO;
import org.example.klifurapp.entity.Role;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceImplementationTest {
    UserRepository users = mock(UserRepository.class);
    PasswordEncoder passwords = mock(PasswordEncoder.class);
    JwtService jwt = mock(JwtService.class);
    AuthServiceImplementation service;

    @BeforeEach void setup() { service = new AuthServiceImplementation(users, passwords, jwt); }

    @Test void registrationHashesPasswordAndAssignsUserRole() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setUsername("anna"); dto.setEmail("anna@example.com"); dto.setPassword("secret123");
        when(passwords.encode("secret123")).thenReturn("hashed");
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        User result = service.register(dto);
        assertEquals(Role.USER, result.getRole());
        assertEquals("hashed", result.getPasswordHash());
        assertNotEquals("secret123", result.getPasswordHash());
        verify(users).save(any(User.class));
    }

    @Test void registrationRejectsDuplicateEmail() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setUsername("anna"); dto.setEmail("taken@example.com"); dto.setPassword("secret123");
        when(users.findByEmail("taken@example.com")).thenReturn(Optional.of(new User()));
        assertThrows(IllegalArgumentException.class, () -> service.register(dto));
        verify(users, never()).save(any());
    }

    @Test void registrationRejectsDuplicateUsername() {
        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setUsername("taken"); dto.setEmail("new@example.com"); dto.setPassword("secret123");
        when(users.findByUsername("taken")).thenReturn(Optional.of(new User()));
        assertThrows(IllegalArgumentException.class, () -> service.register(dto));
        verify(users, never()).save(any());
    }

    @Test void loginReturnsTokenForValidCredentials() {
        User user = new User(); user.setEmail("anna@example.com"); user.setPasswordHash("hashed");
        when(users.findByEmail("anna@example.com")).thenReturn(Optional.of(user));
        when(passwords.matches("secret123", "hashed")).thenReturn(true);
        when(jwt.generateToken(user)).thenReturn("test-token");
        LoginRequestDTO dto = new LoginRequestDTO(); dto.setEmail("anna@example.com"); dto.setPassword("secret123");
        assertEquals("test-token", service.login(dto).getToken());
    }

    @Test void loginRejectsWrongPassword() {
        User user = new User(); user.setEmail("anna@example.com"); user.setPasswordHash("hashed");
        when(users.findByEmail("anna@example.com")).thenReturn(Optional.of(user));
        LoginRequestDTO dto = new LoginRequestDTO(); dto.setEmail("anna@example.com"); dto.setPassword("wrong");
        assertThrows(IllegalArgumentException.class, () -> service.login(dto));
        verify(jwt, never()).generateToken(any());
    }

    @Test void loginRejectsUnknownEmail() {
        LoginRequestDTO dto = new LoginRequestDTO(); dto.setEmail("unknown@example.com"); dto.setPassword("secret");
        assertThrows(IllegalArgumentException.class, () -> service.login(dto));
        verify(jwt, never()).generateToken(any());
    }
}
