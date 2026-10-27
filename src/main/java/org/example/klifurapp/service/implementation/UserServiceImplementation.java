package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.user.UserDTO;
import org.example.klifurapp.DTO.user.UpdateUserDTO;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImplementation(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found, ID mismatch")
                );

        // Hér má síðar bæta við upplýsingum um user sem mætti sækja eins og profile pic
        UserDTO userDto = new UserDTO();
        userDto.setEmail(user.getEmail());
        userDto.setUsername(user.getUsername());

        return userDto;
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found, ID mismatch");
        }

        userRepository.deleteById(id);
    }

    @Override
    public User updateUser(Long id, UpdateUserDTO dto) {
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
}