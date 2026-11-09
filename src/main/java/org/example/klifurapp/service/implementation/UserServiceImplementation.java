package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.AdminUpdateUserDTO;
import org.example.klifurapp.DTO.requests.UpdateUserRequestDTO;
import org.example.klifurapp.DTO.responses.PublicUserProfileResponseDTO;
import org.example.klifurapp.DTO.responses.UserResponseDTO;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.exception.ConflictException;
import org.example.klifurapp.exception.ResourceNotFoundException;
import org.example.klifurapp.repository.ClimbRepository;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClimbRepository climbRepository;

    public UserServiceImplementation(UserRepository userRepository, PasswordEncoder passwordEncoder, ClimbRepository climbRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.climbRepository = climbRepository;
    }

    @Override
    public UserResponseDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        // Hér má síðar bæta við upplýsingum um user sem mætti sækja eins og profile pic
        UserResponseDTO userDto = new UserResponseDTO();
        userDto.setEmail(user.getEmail());
        userDto.setUsername(user.getUsername());

        return userDto;
    }

    @Override
    @Transactional(readOnly = true)
    public PublicUserProfileResponseDTO getPublicUserProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long totalClimbs = climbRepository.countByUserId(userId);

        return new PublicUserProfileResponseDTO(
                user.getId(),
                user.getUsername(),
                totalClimbs
        );
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        userRepository.delete(user);
    }

    @Override
    public User updateUser(Long id, UpdateUserRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found")
                );
        if (dto.getUsername() != null) {
            userRepository.findByUsername(dto.getUsername())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(id)) {
                            throw new ConflictException(
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
                        new ResourceNotFoundException("User not found")
                );
        if (dto.username() != null) {
            userRepository.findByUsername(dto.username())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(id)) {
                            throw new ConflictException(
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
                            throw new ConflictException(
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

}