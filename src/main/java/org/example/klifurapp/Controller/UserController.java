package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
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
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequestDTO dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User successfully deleted");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        LoginResponseDTO responseDto = authService.login(dto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id){
        UserResponseDTO userDto = userService.getUser(id);
        return ResponseEntity.ok(userDto);
    }
    @PutMapping("/{id}")
    public User updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequestDTO dto) {
        return userService.updateUser(id, dto);
    }

}