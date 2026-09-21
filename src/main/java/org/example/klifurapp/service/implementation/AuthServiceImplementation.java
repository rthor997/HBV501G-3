package org.example.klifurapp.service.implementation;


import org.example.klifurapp.DTO.user.CreateUserDTO;
import org.example.klifurapp.DTO.user.LoginRequestDTO;
import org.example.klifurapp.DTO.user.LoginResponseDTO;
import org.example.klifurapp.entity.Role;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImplementation implements AuthService {
    private final UserRepository userRepository;

    public AuthServiceImplementation(
            UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public User register(CreateUserDTO dto) {
        if(userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if(userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }
        User user = new User();

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(dto.getPassword()); // mun breyta til að hafa passwordhasing
        user.setRole(Role.USER);

        return userRepository.save(user);
    }

    @Override
    public LoginResponseDTO authenticate(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email or password")
                );

        if (!user.getPasswordHash().equals(dto.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        LoginResponseDTO responseDto = new LoginResponseDTO();

        responseDto.setEmail(user.getEmail());
        responseDto.setId(user.getId());

        return responseDto;
    }
}
