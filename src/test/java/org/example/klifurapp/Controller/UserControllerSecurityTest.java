package org.example.klifurapp.Controller;

import org.example.klifurapp.config.SecurityConfig;
import org.example.klifurapp.entity.Role;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.security.JwtAuthenticationFilter;
import org.example.klifurapp.service.AuthService;
import org.example.klifurapp.service.JwtService;
import org.example.klifurapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@EnableWebSecurity
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class UserControllerSecurityTest {

    @Autowired MockMvc mvc;

    @MockitoBean UserService userService;
    @MockitoBean AuthService authService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;

    @Test
    void publicRegistrationDoesNotRequireJwt() throws Exception {
        User registered = user(11L, Role.USER);
        registered.setUsername("anna");
        registered.setEmail("anna@example.com");
        when(authService.register(any())).thenReturn(registered);

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"anna","email":"anna@example.com","password":"secret123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("anna"));
    }

    @Test
    void blankRegistrationFieldsReturn400() throws Exception {
        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","email":"","password":""}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(authService);
    }

    @Test
    void publicLoginDoesNotRequireJwt() throws Exception {
        mvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"anna@example.com","password":"secret123"}
                                """))
                .andExpect(status().isOk());
        verify(authService).login(any());
    }

    @Test
    void ordinaryUserCannotCreateAdmin() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        mvc.perform(post("/api/users/admin")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin2","email":"admin2@example.com","password":"secret123"}
                                """))
                .andExpect(status().isForbidden());
        verify(authService, never()).createAdmin(any());
    }

    @Test
    void adminCanCreateAdmin() throws Exception {
        authenticate("admin-token", 22L, Role.ADMIN);
        User newAdmin = user(33L, Role.ADMIN);
        newAdmin.setUsername("admin2");
        when(authService.createAdmin(any())).thenReturn(newAdmin);

        mvc.perform(post("/api/users/admin")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin2","email":"admin2@example.com","password":"secret123"}
                                """))
                .andExpect(status().isCreated());
        verify(authService).createAdmin(any());
    }

    @Test
    void validJwtUsesAuthenticatedUserIdForDeletion() throws Exception {
        authenticate("user-token", 11L, Role.USER);

        mvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isNoContent());
        verify(userService).deleteUser(11L);

    }

    @Test
    void invalidJwtCannotDeleteAccount() throws Exception {
        when(jwtService.isTokenValid("bad-token")).thenReturn(false);

        mvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().is4xxClientError());
        verify(userService, never()).deleteUser(any());
    }

    @Test
    void missingJwtCannotDeleteAccount() throws Exception {
        mvc.perform(delete("/api/users/me"))
                .andExpect(status().is4xxClientError());
        verify(userService, never()).deleteUser(any());
    }

    private void authenticate(String token, Long id, Role role) {
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(id);
        when(userRepository.findById(id))
                .thenReturn(Optional.of(user(id, role)));
    }

    private User user(Long id, Role role) {
        User u = new User();
        org.springframework.test.util.ReflectionTestUtils.setField(u, "id", id);
        u.setRole(role);
        return u;
    }
}
