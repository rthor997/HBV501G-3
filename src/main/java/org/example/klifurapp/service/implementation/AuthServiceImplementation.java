package org.example.klifurapp.service.implementation;


import org.example.klifurapp.DTO.user.CreateUserDTO;
import org.example.klifurapp.DTO.user.LoginRequestDTO;
import org.example.klifurapp.DTO.user.LoginResponseDTO;
import org.example.klifurapp.entity.Role;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.AuthService;
import org.example.klifurapp.service.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImplementation implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public AuthServiceImplementation(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);

        return userRepository.save(user);
    }

    @Override
    public User createAdmin(CreateUserDTO dto) {
        if(userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if(userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User admin = new User();

        admin.setUsername(dto.getUsername());
        admin.setEmail(dto.getEmail());
        admin.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        admin.setRole(Role.ADMIN);
        return userRepository.save(admin);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email or password")
                );

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        String token = jwtService.generateToken(user);
        LoginResponseDTO responseDto = new LoginResponseDTO();

        responseDto.setEmail(user.getEmail());
        responseDto.setToken(token);

        return responseDto;
    }
}
