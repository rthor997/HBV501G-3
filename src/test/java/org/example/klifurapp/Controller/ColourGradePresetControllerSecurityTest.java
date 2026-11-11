package org.example.klifurapp.Controller;

import org.example.klifurapp.DTO.responses.ColourGradePresetResponseDTO;
import org.example.klifurapp.DTO.responses.ColourGradeResponseDTO;
import org.example.klifurapp.config.SecurityConfig;
import org.example.klifurapp.entity.GradingSystem;
import org.example.klifurapp.entity.Role;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.exception.ConflictException;
import org.example.klifurapp.exception.ForbiddenException;
import org.example.klifurapp.exception.ResourceNotFoundException;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.security.JwtAuthenticationFilter;
import org.example.klifurapp.security.SecurityErrorWriter;
import org.example.klifurapp.service.ColourGradeService;
import org.example.klifurapp.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ColourGradePresetController.class)
@EnableWebSecurity
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorWriter.class})
class ColourGradePresetControllerSecurityTest {

    private static final String VALID_GRADE = """
            {"colour":"Blue","gradingSystem":"V_SCALE","minGrade":"V2","maxGrade":"V4"}
            """;

    @Autowired MockMvc mvc;
    @MockitoBean ColourGradeService colourGradeService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;

    @Test
    void userCanCreateColourGrade() throws Exception {
        authenticate();
        when(colourGradeService.createColourGrade(eq(3L), any(), eq(11L))).thenReturn(
                new ColourGradeResponseDTO(7L, 3L, "Blue", GradingSystem.V_SCALE, "V2", "V4"));

        mvc.perform(post("/api/colour-presets/3/colour-grades")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_GRADE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.presetId").value(3))
                .andExpect(jsonPath("$.colour").value("Blue"))
                .andExpect(jsonPath("$.minGrade").value("V2"))
                .andExpect(jsonPath("$.maxGrade").value("V4"));
    }

    @Test
    void anonymousUserCannotCreateColourGrade() throws Exception {
        mvc.perform(post("/api/colour-presets/3/colour-grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_GRADE))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(colourGradeService);
    }

    @Test
    void missingColourReturnsBadRequest() throws Exception {
        authenticate();

        mvc.perform(post("/api/colour-presets/3/colour-grades")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gradingSystem\":\"V_SCALE\",\"minGrade\":\"V2\",\"maxGrade\":\"V4\"}"))
                .andExpect(status().isBadRequest());

        verify(colourGradeService, never()).createColourGrade(anyLong(), any(), anyLong());
    }

    @Test
    void unknownGradingSystemReturnsBadRequest() throws Exception {
        authenticate();

        mvc.perform(post("/api/colour-presets/3/colour-grades")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"colour\":\"Blue\",\"gradingSystem\":\"YDS\",\"minGrade\":\"V2\",\"maxGrade\":\"V4\"}"))
                .andExpect(status().isBadRequest());

        verify(colourGradeService, never()).createColourGrade(anyLong(), any(), anyLong());
    }

    @Test
    void invalidGradeFromServiceReturnsBadRequest() throws Exception {
        authenticate();
        when(colourGradeService.createColourGrade(eq(3L), any(), eq(11L))).thenThrow(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid difficulty grade for V_SCALE"));

        mvc.perform(post("/api/colour-presets/3/colour-grades")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_GRADE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid difficulty grade for V_SCALE"));
    }

    @Test
    void builtInPresetReturnsForbidden() throws Exception {
        authenticate();
        when(colourGradeService.createColourGrade(eq(1L), any(), eq(11L)))
                .thenThrow(new ForbiddenException("Built-in presets cannot be modified"));

        mvc.perform(post("/api/colour-presets/1/colour-grades")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_GRADE))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingPresetReturnsNotFound() throws Exception {
        authenticate();
        when(colourGradeService.createColourGrade(eq(99L), any(), eq(11L)))
                .thenThrow(new ResourceNotFoundException("Preset not found"));

        mvc.perform(post("/api/colour-presets/99/colour-grades")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_GRADE))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicateColourReturnsConflict() throws Exception {
        authenticate();
        when(colourGradeService.createColourGrade(eq(3L), any(), eq(11L)))
                .thenThrow(new ConflictException("This preset already has a grade for colour Blue"));

        mvc.perform(post("/api/colour-presets/3/colour-grades")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_GRADE))
                .andExpect(status().isConflict());
    }

    @Test
    void userCanListColourGrades() throws Exception {
        authenticate();
        when(colourGradeService.getColourGrades(1L, 11L)).thenReturn(List.of(
                new ColourGradeResponseDTO(7L, 1L, "White", GradingSystem.V_SCALE, "V0", "V0")));

        mvc.perform(get("/api/colour-presets/1/colour-grades")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].colour").value("White"));
    }

    @Test
    void anonymousUserCannotListPresets() throws Exception {
        mvc.perform(get("/api/colour-presets"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(colourGradeService);
    }

    @Test
    void userCanListPresets() throws Exception {
        authenticate();
        when(colourGradeService.getPresets(11L)).thenReturn(List.of(
                new ColourGradePresetResponseDTO(1L, "Klifurhúsið", true, List.of())));

        mvc.perform(get("/api/colour-presets").header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Klifurhúsið"))
                .andExpect(jsonPath("$[0].builtIn").value(true));
    }

    @Test
    void userCanCreatePreset() throws Exception {
        authenticate();
        when(colourGradeService.createPreset(any(), eq(11L))).thenReturn(
                new ColourGradePresetResponseDTO(3L, "Mitt gym", false, List.of()));

        mvc.perform(post("/api/colour-presets")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mitt gym\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.builtIn").value(false));
    }

    @Test
    void blankPresetNameReturnsBadRequest() throws Exception {
        authenticate();

        mvc.perform(post("/api/colour-presets")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest());

        verify(colourGradeService, never()).createPreset(any(), anyLong());
    }

    @Test
    void userCanSelectPreset() throws Exception {
        authenticate();
        when(colourGradeService.selectPreset(1L, 11L)).thenReturn(
                new ColourGradePresetResponseDTO(1L, "Klifurhúsið", true, List.of()));

        mvc.perform(put("/api/colour-presets/selected")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"presetId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void selectedPresetReturnsPresetWhenSet() throws Exception {
        authenticate();
        when(colourGradeService.getSelectedPreset(11L)).thenReturn(Optional.of(
                new ColourGradePresetResponseDTO(1L, "Klifurhúsið", true, List.of())));

        mvc.perform(get("/api/colour-presets/selected").header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Klifurhúsið"));
    }

    @Test
    void selectedPresetReturnsNoContentWhenNotSet() throws Exception {
        authenticate();
        when(colourGradeService.getSelectedPreset(11L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/colour-presets/selected").header("Authorization", "Bearer user-token"))
                .andExpect(status().isNoContent());
    }

    private void authenticate() {
        when(jwtService.isTokenValid("user-token")).thenReturn(true);
        when(jwtService.extractUserId("user-token")).thenReturn(11L);
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 11L);
        user.setRole(Role.USER);
        when(userRepository.findById(11L)).thenReturn(Optional.of(user));
    }
}
