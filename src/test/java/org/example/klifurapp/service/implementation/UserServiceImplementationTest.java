package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.AdminUpdateUserDTO;
import org.example.klifurapp.DTO.requests.UpdateUserRequestDTO;
import org.example.klifurapp.DTO.responses.UserResponseDTO;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.exception.ResourceNotFoundException;
import org.example.klifurapp.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.example.klifurapp.DTO.responses.PublicUserProfileResponseDTO;
import org.example.klifurapp.repository.ClimbRepository;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplementationTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ClimbRepository climbRepository;
    @InjectMocks UserServiceImplementation service;

    @Test
    void getUserReturnsResponseDto() {
        User user = user("anna", "anna@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDTO result = service.getUser(1L);

        assertEquals("anna", result.getUsername());
        assertEquals("anna@example.com", result.getEmail());
    }

    @Test
    void getMissingUserThrows() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getUser(99L));
    }

    @Test
    void deleteExistingUser() {
        User user = user("anna", "anna@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        service.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteMissingUserDoesNotCallDelete() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteUser(99L));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void updateUsernameAndPasswordHashesPassword() {
        User user = user("old", "anna@example.com");
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO();
        dto.setUsername("new");
        dto.setPassword("plain-password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("new")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-password")).thenReturn("hashed-password");
        when(userRepository.save(user)).thenReturn(user);

        User result = service.updateUser(1L, dto);

        assertEquals("new", result.getUsername());
        assertEquals("hashed-password", result.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void updateRejectsDuplicateUsername() {
        User user = user("old", "anna@example.com");
        User other = user("taken", "other@example.com");
        // Unsaved User instances have null IDs, so use repository mocks
        // to distinguish the existing account from the requested account.
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("taken")).thenReturn(Optional.of(other));
        UpdateUserRequestDTO dto = new UpdateUserRequestDTO();
        dto.setUsername("taken");

        // Current implementation compares IDs; assign IDs through reflection.
        setId(user, 1L);
        setId(other, 2L);
        assertThrows(ConflictException.class, () -> service.updateUser(1L, dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void adminCanUpdateEmailAndPassword() {
        User user = user("anna", "old@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("new-password")).thenReturn("encoded");
        when(userRepository.save(user)).thenReturn(user);

        User result = service.updateUserAsAdmin(
                1L, new AdminUpdateUserDTO(null, "new@example.com", "new-password"));

        assertEquals("new@example.com", result.getEmail());
        assertEquals("encoded", result.getPasswordHash());
    }

    @Test
    void adminUpdateRejectsDuplicateEmail() {
        User user = user("anna", "old@example.com");
        User other = user("other", "taken@example.com");
        setId(user, 1L);
        setId(other, 2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(other));

        assertThrows(ConflictException.class, () ->
                service.updateUserAsAdmin(1L,
                        new AdminUpdateUserDTO(null, "taken@example.com", null)));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getPublicProfileReturnsCorrectInformation() {
        User user = user("anna", "anna@example.com");
        setId(user, 5L);

        when(userRepository.findById(5L))
                .thenReturn(Optional.of(user));
        when(climbRepository.countByUserId(5L))
                .thenReturn(12L);

        PublicUserProfileResponseDTO result =
                service.getPublicUserProfile(5L);

        assertEquals(5L, result.id());
        assertEquals("anna", result.username());
        assertEquals(12L, result.totalClimbs());
    }

    @Test
    void getPublicProfileReturns404ForMissingUser() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getPublicUserProfile(999L)
        );

        assertEquals(404, exception.getStatusCode().value());

        verifyNoInteractions(climbRepository);
    }

    @Test
    void getPublicProfileReturnsZeroClimbs() {
        User user = user("anna", "anna@example.com");
        setId(user, 5L);

        when(userRepository.findById(5L))
                .thenReturn(Optional.of(user));

        PublicUserProfileResponseDTO result =
                service.getPublicUserProfile(5L);

        assertEquals(0L, result.totalClimbs());
    }

    private static User user(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }

    private static void setId(User user, Long id) {
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", id);
    }
}
