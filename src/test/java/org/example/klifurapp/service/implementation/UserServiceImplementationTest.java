package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.AdminUpdateUserDTO;
import org.example.klifurapp.DTO.requests.UpdateUserRequestDTO;
import org.example.klifurapp.DTO.responses.UserResponseDTO;
import org.example.klifurapp.DTO.responses.UserSearchResponseDTO;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.exception.ResourceNotFoundException;
import org.example.klifurapp.exception.ConflictException;
import org.example.klifurapp.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.example.klifurapp.service.ImageStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.example.klifurapp.DTO.responses.PublicUserProfileResponseDTO;
import org.example.klifurapp.repository.ClimbRepository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplementationTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ClimbRepository climbRepository;
    @Mock ImageStorageService imageStorage;
    UserServiceImplementation service;

    @BeforeEach
    void setup() {
        service = new UserServiceImplementation(
                userRepository,
                passwordEncoder,
                imageStorage,
                climbRepository,
                List.of(
                        new NewestUserFirstStrategy(),
                        new OldestUserFirstStrategy(),
                        new AlphabeticalUserStrategy(),
                        new ReverseAlphabeticalUserStrategy()
                )
        );
    }

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
    void deletingUserAlsoDeletesProfilePicture() throws Exception {
        User user = user("anna", "anna@example.com");
        user.setProfilePictureFilename("profile.png");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        service.deleteUser(1L);

        var order = inOrder(userRepository, imageStorage);

        order.verify(userRepository).delete(user);
        order.verify(userRepository).flush();
        order.verify(imageStorage).delete("profile.png");
    }

    @Test
    void deletingUserWithoutProfilePictureDoesNotDeleteFile()
            throws Exception {

        User user = user("anna", "anna@example.com");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        service.deleteUser(1L);

        verify(userRepository).delete(user);
        verify(userRepository).flush();
        verifyNoInteractions(imageStorage);
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

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.getPublicUserProfile(999L)
        );

        assertEquals("User not found", exception.getMessage());

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

    @Test
    void searchUsersReturnsMatchingUserDTOs() {
        User anna = user("anna", "anna@example.com");
        setId(anna, 1L);

        PageRequest pageable = PageRequest.of(0, 20);
        PageRequest sortedPageable = PageRequest.of(
                0, 20, Sort.by(
                        Sort.Order.desc("id")
                )
        );

        when(userRepository.findByUsernameContainingIgnoreCase(
                eq("anna"), eq(sortedPageable)
        )).thenReturn(new PageImpl<>(List.of(anna), sortedPageable, 1));

        Page<UserSearchResponseDTO> result =
                service.searchUsers("anna", "newestUser", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).id());
        assertEquals("anna", result.getContent().get(0).username());
    }

    @Test
    void searchUsersUsesAlphabeticalStrategy() {
        PageRequest pageable = PageRequest.of(0, 20);

        PageRequest expectedPageable = PageRequest.of(
                0, 20,
                Sort.by(
                        Sort.Order.asc("username"),
                        Sort.Order.asc("id")
                )
        );

        when(userRepository.findByUsernameContainingIgnoreCase(
                eq(""), eq(expectedPageable)
        )).thenReturn(Page.empty(expectedPageable));

        service.searchUsers("", "alphabetical", pageable);

        verify(userRepository).findByUsernameContainingIgnoreCase(
                eq(""), eq(expectedPageable)
        );
    }

    @Test
    void searchUsersPreservesPagination() {
        PageRequest pageable = PageRequest.of(2, 5);

        PageRequest expectedPageable = PageRequest.of(
                2, 5,
                Sort.by(Sort.Order.desc("id"))
        );

        when(userRepository.findByUsernameContainingIgnoreCase(
                eq("anna"), eq(expectedPageable)
        )).thenReturn(Page.empty(expectedPageable));

        Page<UserSearchResponseDTO> result =
                service.searchUsers("anna", "newestUser", pageable);

        assertEquals(2, result.getNumber());
        assertEquals(5, result.getSize());

        verify(userRepository).findByUsernameContainingIgnoreCase(
                eq("anna"), eq(expectedPageable)
        );
    }

    @Test
    void searchUsersReturnsEmptyPageWhenNoMatches() {
        PageRequest pageable = PageRequest.of(0, 20);

        PageRequest sortedPageable = PageRequest.of(
                0, 20, Sort.by(Sort.Order.desc("id"))
        );

        when(userRepository.findByUsernameContainingIgnoreCase(
                eq("nonexistent"), eq(sortedPageable)
        )).thenReturn(Page.empty(sortedPageable));

        Page<UserSearchResponseDTO> result =
                service.searchUsers("nonexistent", "newestUser", pageable);

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void searchUsersRejectsInvalidSortingStrategy() {
        PageRequest pageable = PageRequest.of(0, 20);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.searchUsers("anna", "invalid", pageable)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(userRepository, never())
                .findByUsernameContainingIgnoreCase(
                        anyString(), any()
                );
    }
  
    @Test
    void replacingProfilePictureDeletesOldFile() throws Exception {
        User user = new User();
        user.setProfilePictureFilename("old-picture.png");

        MockMultipartFile file = new MockMultipartFile(
                "file", "new.png", "image/png", new byte[]{1, 2, 3}
        );

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));
        when(imageStorage.store(file))
                .thenReturn("new-picture.png");

        service.updateProfilePicture(10L, file);

        assertEquals("new-picture.png", user.getProfilePictureFilename());

        var order = inOrder(imageStorage, userRepository);
        order.verify(imageStorage).store(file);
        order.verify(userRepository).saveAndFlush(user);
        order.verify(imageStorage).delete("old-picture.png");
    }

    @Test
    void databaseFailureDeletesNewlyUploadedFile() throws Exception {
        User user = new User();
        user.setProfilePictureFilename("old-picture.png");

        MockMultipartFile file = new MockMultipartFile(
                "file", "new.png", "image/png", new byte[]{1, 2, 3}
        );

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));
        when(imageStorage.store(file))
                .thenReturn("new-picture.png");
        when(userRepository.saveAndFlush(user))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(
                RuntimeException.class,
                () -> service.updateProfilePicture(10L, file)
        );

        verify(imageStorage).delete("new-picture.png");
        verify(imageStorage, never()).delete("old-picture.png");
    }

    @Test
    void imageStorageFailureReturns500() throws Exception {
        User user = new User();

        MockMultipartFile file = new MockMultipartFile(
                "file", "new.png", "image/png", new byte[]{1, 2, 3}
        );

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));
        when(imageStorage.store(file))
                .thenThrow(new IOException("Disk error"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateProfilePicture(10L, file)
        );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getStatusCode()
        );

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void deletingProfilePictureRemovesReferenceAndFile() throws Exception {
        User user = new User();
        user.setProfilePictureFilename("profile.png");

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        service.deleteProfilePicture(10L);

        assertNull(user.getProfilePictureFilename());

        var order = inOrder(userRepository, imageStorage);
        order.verify(userRepository).saveAndFlush(user);
        order.verify(imageStorage).delete("profile.png");
    }

    @Test
    void deletingNonexistentProfilePictureDoesNothing() {
        User user = new User();

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        assertDoesNotThrow(
                () -> service.deleteProfilePicture(10L)
        );

        verify(userRepository, never()).saveAndFlush(any(User.class));
        verifyNoInteractions(imageStorage);
    }

    @Test
    void imageDeletionFailureDoesNotFailRequest() throws Exception {
        User user = new User();
        user.setProfilePictureFilename("profile.png");

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        doThrow(new IOException("Could not delete file"))
                .when(imageStorage).delete("profile.png");

        assertDoesNotThrow(
                () -> service.deleteProfilePicture(10L)
        );

        assertNull(user.getProfilePictureFilename());
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void getProfilePictureFilenameReturnsFilename() {
        User user = new User();
        user.setProfilePictureFilename("profile.png");

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        String filename = service.getProfilePictureFilename(10L);

        assertEquals("profile.png", filename);
    }

    @Test
    void getProfilePictureFilenameMissingUserReturns404() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getProfilePictureFilename(999L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
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
