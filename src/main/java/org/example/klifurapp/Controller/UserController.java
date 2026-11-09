package org.example.klifurapp.Controller;

import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import org.example.klifurapp.DTO.requests.AdminUpdateUserDTO;
import org.example.klifurapp.DTO.responses.UserResponseDTO;
import org.example.klifurapp.DTO.requests.CreateUserRequestDTO;
import org.example.klifurapp.DTO.requests.LoginRequestDTO;
import org.example.klifurapp.DTO.responses.LoginResponseDTO;
import org.example.klifurapp.DTO.requests.UpdateUserRequestDTO;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.service.AuthService;
import org.example.klifurapp.service.ImageStorageService;
import org.example.klifurapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final ImageStorageService imageStorageService;

    public UserController(UserService userService,
                          AuthService authService,
                          ImageStorageService imageStorageService) {
        this.userService = userService;
        this.authService = authService;
        this.imageStorageService = imageStorageService;
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

    @PutMapping(
            value = "/me/profile-picture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> updateProfilePicture(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        userService.updateProfilePicture(userId, file);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/profile-picture")
    public ResponseEntity<Void> deleteProfilePicture(
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        userService.deleteProfilePicture(userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/profile-picture")
    public ResponseEntity<Resource> getProfilePicture(
            @PathVariable Long id
    ) throws IOException {

        String filename = userService.getProfilePictureFilename(id);

        if (filename == null) {
            return ResponseEntity.notFound().build();
        }

        Resource image = imageStorageService.load(filename);

        MediaType mediaType = filename.endsWith(".png")
                ? MediaType.IMAGE_PNG
                : MediaType.IMAGE_JPEG;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(image);
    }
}