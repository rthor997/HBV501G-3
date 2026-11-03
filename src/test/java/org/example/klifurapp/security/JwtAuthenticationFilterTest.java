package org.example.klifurapp.security;

import org.example.klifurapp.entity.Role;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.FilterChain;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private UserRepository userRepository;
    private JwtAuthenticationFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtService = mock(JwtService.class);
        userRepository = mock(UserRepository.class);
        chain = mock(FilterChain.class);
        filter = new JwtAuthenticationFilter(jwtService, userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validUserTokenAuthenticatesCorrectUserAndRole() throws Exception {
        givenValidToken("user-token", 11L, Role.USER);
        runFilter("Bearer user-token");

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals(11L, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        verify(jwtService).extractUserId("user-token");
        verify(userRepository).findById(11L);
        verify(chain).doFilter(any(), any());
    }

    @Test
    void validAdminTokenAuthenticatesWithAdminRole() throws Exception {
        givenValidToken("admin-token", 22L, Role.ADMIN);
        runFilter("Bearer admin-token");

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(22L, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        verify(chain).doFilter(any(), any());
    }

    @Test
    void invalidTokenDoesNotAuthenticate() throws Exception {
        when(jwtService.isTokenValid("bad-token")).thenReturn(false);
        runFilter("Bearer bad-token");

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService).isTokenValid("bad-token");
        verify(jwtService, never()).extractUserId(anyString());
        verifyNoInteractions(userRepository);
        verify(chain).doFilter(any(), any());
    }

    @Test
    void expiredTokenDoesNotAuthenticateWhenJwtServiceRejectsIt() throws Exception {
        // Expiration is checked by JwtService; the filter receives its false result.
        when(jwtService.isTokenValid("expired-token")).thenReturn(false);
        runFilter("Bearer expired-token");

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService).isTokenValid("expired-token");
        verifyNoInteractions(userRepository);
        verify(chain).doFilter(any(), any());
    }

    @Test
    void missingAuthorizationHeaderSkipsJwtLookup() throws Exception {
        runFilter(null);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, userRepository);
        verify(chain).doFilter(any(), any());
    }

    @Test
    void nonBearerAuthorizationHeaderSkipsJwtLookup() throws Exception {
        runFilter("Basic abc123");

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, userRepository);
        verify(chain).doFilter(any(), any());
    }

    @Test
    void validTokenForDeletedUserDoesNotAuthenticate() throws Exception {
        when(jwtService.isTokenValid("deleted-user-token")).thenReturn(true);
        when(jwtService.extractUserId("deleted-user-token")).thenReturn(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        runFilter("Bearer deleted-user-token");

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userRepository).findById(99L);
        verify(chain).doFilter(any(), any());
    }

    @Test
    void existingAuthenticationIsNotOverwritten() throws Exception {
        var existing = new UsernamePasswordAuthenticationToken(
                77L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(existing);
        givenValidToken("admin-token", 22L, Role.ADMIN);
        runFilter("Bearer admin-token");

        assertSame(existing, SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(any(), any());
    }

    private void givenValidToken(String token, Long userId, Role role) {
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(userId);
        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);
        user.setRole(role);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    private void runFilter(String authorizationHeader) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/users/me");
        if (authorizationHeader != null) {
            request.addHeader("Authorization", authorizationHeader);
        }
        filter.doFilter(request, new MockHttpServletResponse(), chain);
    }
}
