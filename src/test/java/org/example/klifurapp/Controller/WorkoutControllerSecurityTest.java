package org.example.klifurapp.Controller;

import org.example.klifurapp.config.SecurityConfig;
import org.example.klifurapp.entity.Role;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.security.JwtAuthenticationFilter;
import org.example.klifurapp.security.SecurityErrorWriter;
import org.example.klifurapp.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.example.klifurapp.service.WorkoutService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(WorkoutController.class)
@EnableWebSecurity
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorWriter.class})
class WorkoutControllerSecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean WorkoutService workoutService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;

    @Test void authenticatedUserCanLogWorkout() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        mvc.perform(post("/api/workouts").header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON).content(""" 
                    {"exerciseName":"Pull-ups","sets":3,"repetitions":8}
                    """))
                .andExpect(status().isCreated());
        verify(workoutService).logWorkout(any(), eq(11L));
    }
    @Test void missingExerciseNameReturns400() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        mvc.perform(post("/api/workouts").header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON).content("{\"sets\":3}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(workoutService);
    }
    @Test void unauthenticatedUserCannotLogWorkout() throws Exception {
        mvc.perform(post("/api/workouts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"exerciseName\":\"Pull-ups\"}"))
                .andExpect(status().is4xxClientError());
        verifyNoInteractions(workoutService);
    }
    @Test void ownerCanEditWorkout() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        mvc.perform(put("/api/workouts/42").header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON).content("{\"exerciseName\":\"Squat\"}"))
                .andExpect(status().isOk());
        verify(workoutService).updateWorkout(eq(42L), any(), eq(11L));
    }
    @Test void ownerCanDeleteWorkout() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        mvc.perform(delete("/api/workouts/42").header("Authorization", "Bearer user-token"))
                .andExpect(status().isNoContent());
        verify(workoutService).deleteWorkout(42L, 11L);
    }
    @Test void deletingAnotherUsersWorkoutReturns403() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(workoutService).deleteWorkout(42L, 11L);
        mvc.perform(delete("/api/workouts/42").header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }
    @Test void paginationUsesRequestedPageSizeAndDescendingId() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        when(workoutService.getWorkoutsByUserId(eq(11L), any(Pageable.class)))
                .thenAnswer(inv -> Page.empty(inv.getArgument(1)));
        mvc.perform(get("/api/workouts/user/11?page=1&size=5")
                .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(workoutService).getWorkoutsByUserId(eq(11L), captor.capture());
        assertEquals(1, captor.getValue().getPageNumber());
        assertEquals(5, captor.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, captor.getValue().getSort().getOrderFor("id").getDirection());
    }
    @Test void invalidPaginationReturns400() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        mvc.perform(get("/api/workouts/user/11?page=0&size=101")
                .header("Authorization", "Bearer user-token"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(workoutService);
    }

    private void authenticate(String token, Long id, Role role) {
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(id);
        User user = new User();
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", id);
        user.setRole(role);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
    }
}