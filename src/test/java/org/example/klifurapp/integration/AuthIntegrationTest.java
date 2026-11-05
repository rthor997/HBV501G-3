package org.example.klifurapp.integration;

import org.example.klifurapp.DTO.requests.CreateUserRequestDTO;
import org.example.klifurapp.entity.Role;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {
    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private CreateUserRequestDTO request(String username, String email) {
        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        ReflectionTestUtils.setField(dto, "username", username);
        ReflectionTestUtils.setField(dto, "email", email);
        ReflectionTestUtils.setField(dto, "password", "secret123");
        return dto;
    }

    @Test void registrationPersistsUserAndHashesPassword() {
        User registered = authService.register(request("anna", "anna@example.com"));
        assertNotNull(registered.getId());
        User saved = userRepository.findById(registered.getId()).orElseThrow();
        assertEquals("anna", saved.getUsername());
        assertEquals("anna@example.com", saved.getEmail());
        assertEquals(Role.USER, saved.getRole());
        assertNotEquals("secret123", saved.getPasswordHash());
        assertTrue(passwordEncoder.matches("secret123", saved.getPasswordHash()));
    }

    @Test void duplicateUsernameIsRejectedWithoutSavingSecondUser() {
        authService.register(request("anna", "anna@example.com"));
        long before = userRepository.count();
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> authService.register(request("anna", "different@example.com")));
        assertEquals("Username is already taken", error.getMessage());
        assertEquals(before, userRepository.count());
    }

    @Test void duplicateEmailIsRejectedWithoutSavingSecondUser() {
        authService.register(request("anna", "anna@example.com"));
        long before = userRepository.count();
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> authService.register(request("different", "anna@example.com")));
        assertEquals("Email is already registered", error.getMessage());
        assertEquals(before, userRepository.count());
    }

    @Test void createAdminPersistsAdminRole() {
        User admin = authService.createAdmin(request("admin", "admin@example.com"));
        assertEquals(Role.ADMIN, userRepository.findById(admin.getId()).orElseThrow().getRole());
    }
}
