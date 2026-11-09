package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.AdminUpdateUserDTO;
import org.example.klifurapp.DTO.requests.UpdateUserRequestDTO;
import org.example.klifurapp.DTO.responses.UserResponseDTO;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.ImageStorageService;
import org.example.klifurapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.awt.*;
import java.io.IOException;

@Service
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageStorageService imageStorage;

    public UserServiceImplementation(UserRepository userRepository, PasswordEncoder passwordEncoder
    , ImageStorageService imageStorage) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.imageStorage = imageStorage;
    }

    @Override
    public UserResponseDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found, ID mismatch")
                );

        // Hér má síðar bæta við upplýsingum um user sem mætti sækja eins og profile pic
        UserResponseDTO userDto = new UserResponseDTO();
        userDto.setEmail(user.getEmail());
        userDto.setUsername(user.getUsername());

        return userDto;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found, ID mismatch")
                );

        userRepository.delete(user);
    }

    @Override
    public User updateUser(Long id, UpdateUserRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(()->
                        new IllegalArgumentException("User Not Found")
                );
        if (dto.getUsername() != null) {
            userRepository.findByUsername(dto.getUsername())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(id)) {
                            throw new IllegalArgumentException(
                                    "Username is already taken"
                            );
                        }
                    });
            user.setUsername(dto.getUsername());
        }

        if (dto.getPassword() != null) {
            user.setPasswordHash(
                    passwordEncoder.encode(dto.getPassword())
            );
        }
        return userRepository.save(user);
    }

    @Override
    public User updateUserAsAdmin(Long id, AdminUpdateUserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(()->
                        new IllegalArgumentException("User Not Found")
                );
        if (dto.username() != null) {
            userRepository.findByUsername(dto.username())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(id)) {
                            throw new IllegalArgumentException(
                                    "Username is already taken"
                            );
                        }
                    });
            user.setUsername(dto.username());
        }

        if (dto.email() != null) {
            userRepository.findByEmail(dto.email())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(id)) {
                            throw new IllegalArgumentException(
                                    "Email is already registered"
                            );
                        }
                    });

            user.setEmail(dto.email());
        }

        if (dto.password() != null) {
            user.setPasswordHash(
                    passwordEncoder.encode(dto.password())
            );
        }
        return userRepository.save(user);
    }

    @Override
    public String updateProfilePicture(
            Long userId,
            MultipartFile file
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        String oldFilename = user.getProfilePictureFilename();
        String newFilename;

        try {
            newFilename = imageStorage.store(file);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save image", e
            );
        }

        try {
            user.setProfilePictureFilename(newFilename);
            userRepository.saveAndFlush(user);
        } catch (RuntimeException e) {

            // Remove the newly uploaded file if database update fails.
            try {
                imageStorage.delete(newFilename);
            } catch (IOException cleanupError) {
                e.addSuppressed(cleanupError);
            }

            throw e;
        }

        // Remove previous image only after updating the reference.
        if (oldFilename != null) {
            try {
                imageStorage.delete(oldFilename);
            } catch (IOException e) {
                // Log the cleanup failure.
                // The new image is still associated with the user.
            }
        }

        return newFilename;
    }

    @Override
    public void deleteProfilePicture(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        String filename = user.getProfilePictureFilename();

        if (filename == null) {
            return;
        }

        user.setProfilePictureFilename(null);
        userRepository.saveAndFlush(user);

        try {
            imageStorage.delete(filename);
        } catch (IOException e) {
            // Log and arrange retry if file deletion fails.
        }
    }

    @Override
    public String getProfilePictureFilename(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        return user.getProfilePictureFilename();
    }
}