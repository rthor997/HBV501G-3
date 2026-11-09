package org.example.klifurapp.Controller;

import org.example.klifurapp.config.SecurityConfig;
import org.example.klifurapp.entity.Role;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.security.JwtAuthenticationFilter;
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
import org.example.klifurapp.service.RouteService;

@WebMvcTest(RouteController.class)
@EnableWebSecurity
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class RouteControllerSecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean RouteService routeService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;

    @Test void authenticatedUserCanRequestRouteDetails() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        mvc.perform(get("/api/routes/42").header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk());
        verify(routeService).getRouteDetails(42L, 11L);
    }
    @Test void anonymousUserCannotRequestRouteDetails() throws Exception {
        mvc.perform(get("/api/routes/42")).andExpect(status().is4xxClientError());
        verifyNoInteractions(routeService);
    }
    @Test void ordinaryUserCannotAdminUpdateRoute() throws Exception {
        authenticate("user-token", 11L, Role.USER);
        mvc.perform(put("/api/routes/admin/42").header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        verify(routeService, never()).updateRouteAsAdmin(anyLong(), any());
    }
    @Test void adminCanUpdateRoute() throws Exception {
        authenticate("admin-token", 22L, Role.ADMIN);
        mvc.perform(put("/api/routes/admin/42").header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
        verify(routeService).updateRouteAsAdmin(eq(42L), any());
    }

    private void authenticate(String token, Long id, Role role) {
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(id);
        User user = new User();
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", id);
        user.setRole(role);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
    }

    // UC18
    @Test
    void ordinaryUserCannotDeleteRoute() throws Exception {
        authenticate("user-token", 11L, Role.USER);

        mvc.perform(
                        delete("/api/routes/admin/42")
                                .header("Authorization", "Bearer user-token")
                )
                .andExpect(status().isForbidden());

        verify(routeService, never())
                .deleteRouteAsAdmin(anyLong());
    }

    @Test
    void adminCanDeleteRoute() throws Exception {
        authenticate("admin-token", 22L, Role.ADMIN);

        mvc.perform(
                        delete("/api/routes/admin/42")
                                .header("Authorization", "Bearer admin-token")
                )
                .andExpect(status().isNoContent());

        verify(routeService).deleteRouteAsAdmin(42L);
    }

    @Test
    void anonymousUserCannotDeleteRoute() throws Exception {
        mvc.perform(
                        delete("/api/routes/admin/42")
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(routeService);
    }


}