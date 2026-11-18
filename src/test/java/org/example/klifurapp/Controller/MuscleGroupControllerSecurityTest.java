package org.example.klifurapp.Controller;

import org.example.klifurapp.DTO.responses.MuscleGroupResponseDTO;
import org.example.klifurapp.config.SecurityConfig;
import org.example.klifurapp.entity.Role;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.security.JwtAuthenticationFilter;
import org.example.klifurapp.security.SecurityErrorWriter;
import org.example.klifurapp.service.JwtService;
import org.example.klifurapp.service.MuscleGroupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MuscleGroupController.class)
@EnableWebSecurity
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorWriter.class})
class MuscleGroupControllerSecurityTest {

    @Autowired MockMvc mvc;
    @MockitoBean MuscleGroupService muscleGroupService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;

    @Test
    void userCanListMuscleGroups() throws Exception {
        when(jwtService.isTokenValid("user-token")).thenReturn(true);
        when(jwtService.extractUserId("user-token")).thenReturn(11L);
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 11L);
        user.setRole(Role.USER);
        when(userRepository.findById(11L)).thenReturn(Optional.of(user));
        when(muscleGroupService.getAll()).thenReturn(List.of(new MuscleGroupResponseDTO(1L, "Fingers")));

        mvc.perform(get("/api/muscle-groups").header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Fingers"));
    }

    @Test
    void anonymousUserCannotListMuscleGroups() throws Exception {
        mvc.perform(get("/api/muscle-groups"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(muscleGroupService);
    }
}
