package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
import org.example.klifurapp.DTO.requests.AdminUpdateUserDTO;
import org.example.klifurapp.DTO.responses.UserResponseDTO;
import org.example.klifurapp.DTO.requests.CreateUserRequestDTO;
import org.example.klifurapp.DTO.requests.LoginRequestDTO;
import org.example.klifurapp.DTO.responses.LoginResponseDTO;
import org.example.klifurapp.DTO.requests.UpdateUserRequestDTO;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.service.AuthService;
import org.example.klifurapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }
    //public endpoint
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequestDTO dto) {
        User user = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // creates a new account if you're an admin
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createAdmin(
            @Valid @RequestBody CreateUserRequestDTO dto) {
        User admin = authService.createAdmin(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(admin);
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
    // public endpoint
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        LoginResponseDTO responseDto = authService.login(dto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getUser(Authentication authentication){
        Long userId = (Long) authentication.getPrincipal();
        UserResponseDTO userDto = userService.getUser(userId);
        return ResponseEntity.ok(userDto);
    }
    @PutMapping("/me")
    public ResponseEntity<User> updateUser(
            @RequestBody UpdateUserRequestDTO dto,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        User updatedUser = userService.updateUser(userId, dto);

        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUserAsAdmin(
            @PathVariable Long userId,
            @RequestBody AdminUpdateUserDTO dto) {
        User updatedUser = userService.updateUserAsAdmin(userId, dto);
        return ResponseEntity.ok(updatedUser);
    }
}