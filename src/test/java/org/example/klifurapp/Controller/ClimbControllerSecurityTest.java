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
import org.example.klifurapp.service.ClimbService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(ClimbController.class)
@EnableWebSecurity
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorWriter.class})
class ClimbControllerSecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean ClimbService climbService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;

    private static final String VALID_CLIMB = """
            {"gradingSystem":"V_SCALE","difficultyGrade":"V3", "climbingStyle":"BOULDERING","date":"2026-11-03"}
            """;

    @Test void authenticatedUserCanCreateClimb() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        mvc.perform(post("/api/climbs").header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON).content(VALID_CLIMB))
                .andExpect(status().isCreated());
        verify(climbService).createClimb(any(), eq(11L));
    }
    @Test void unauthenticatedUserCannotCreateClimb() throws Exception {
        mvc.perform(post("/api/climbs").contentType(MediaType.APPLICATION_JSON).content(VALID_CLIMB))
                .andExpect(status().is4xxClientError());
        verifyNoInteractions(climbService);
    }
    @Test void missingGradeReturns400() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        mvc.perform(post("/api/climbs").header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""" 
                    {"gradingSystem":"V_SCALE","climbingStyle":"BOULDERING","date":"2026-11-03"}
                    """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(climbService);
    }
    @Test void ownerCanDeleteClimb() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        mvc.perform(delete("/api/climbs/42").header("Authorization", "Bearer user-token"))
                .andExpect(status().isNoContent());
        verify(climbService).deleteClimb(42L, 11L);
    }
    @Test void deletingAnotherUsersClimbReturns403() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(climbService).deleteClimb(42L, 11L);
        mvc.perform(delete("/api/climbs/42").header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }
    @Test void ordinaryUserCannotAdminUpdateClimb() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        mvc.perform(put("/api/climbs/admin/42").header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        verify(climbService, never()).updateClimbAsAdmin(anyLong(), any());
    }
    @Test void adminCanUpdateClimb() throws Exception {
        authenticate("admin-token", 22L, Role.ADMIN);
        mvc.perform(put("/api/climbs/admin/42").header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
        verify(climbService).updateClimbAsAdmin(eq(42L), any());
    }
    @Test void paginationUsesRequestedPageSizeAndDescendingSort() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        when(climbService.getClimbsByUserId(eq(11L), any(Pageable.class)))
                .thenAnswer(inv -> Page.empty(inv.getArgument(1)));
        mvc.perform(get("/api/climbs/user/11?page=2&size=5")
                .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(climbService).getClimbsByUserId(eq(11L), captor.capture());
        assertEquals(2, captor.getValue().getPageNumber());
        assertEquals(5, captor.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, captor.getValue().getSort().getOrderFor("date").getDirection());
    }
    @Test void invalidPaginationReturns400() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        mvc.perform(get("/api/climbs/user/11?page=-1&size=5")
                .header("Authorization", "Bearer user-token"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(climbService);
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