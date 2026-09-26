package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
import org.example.klifurapp.DTO.user.CreateUserDTO;
import org.example.klifurapp.DTO.user.LoginRequestDTO;
import org.example.klifurapp.DTO.user.LoginResponseDTO;
import org.example.klifurapp.DTO.user.UpdateUserDTO;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.service.AuthService;
import org.example.klifurapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody CreateUserDTO dto) {
        return authService.register(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        LoginResponseDTO responseDto = authService.authenticate(dto);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}")
    public User updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserDTO dto) {
        return userService.updateUser(id, dto);
    }

}