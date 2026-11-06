package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.CreateClimbRequestDTO;
import org.example.klifurapp.entity.*;
import org.example.klifurapp.repository.ClimbRepository;
import org.example.klifurapp.repository.RouteRepository;
import org.example.klifurapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ClimbServiceImplementationTest {
    ClimbRepository climbs = mock(ClimbRepository.class);
    UserRepository users = mock(UserRepository.class);
    RouteRepository routes = mock(RouteRepository.class);
    ClimbServiceImplementation service;
    @BeforeEach void setup() { service = new ClimbServiceImplementation(climbs, users, routes); }

    private User user(long id) throws Exception {
        User user = new User();
        Field f = User.class.getDeclaredField("id"); f.setAccessible(true); f.set(user, id);
        return user;
    }

    @Test void createClimbUsesAuthenticatedUserAndSaves() throws Exception {
        User owner = user(10L);
        when(users.findById(10L)).thenReturn(Optional.of(owner));
        when(climbs.save(any(Climb.class))).thenAnswer(inv -> inv.getArgument(0));
        CreateClimbRequestDTO dto = new CreateClimbRequestDTO();
        dto.setGradingSystem(GradingSystem.V_SCALE); dto.setDifficultyGrade("V3");
        dto.setClimbingStyle(ClimbingStyle.BOULDERING); dto.setDate(LocalDate.of(2026,11,1));
        Climb result = service.createClimb(dto, 10L);
        assertSame(owner, result.getUser());
        assertEquals("V3", result.getDifficultyGrade());
        verify(climbs).save(any(Climb.class));
    }

    @Test void invalidGradeIsRejectedAndNotSaved() throws Exception {
        when(users.findById(10L)).thenReturn(Optional.of(user(10L)));
        CreateClimbRequestDTO dto = new CreateClimbRequestDTO();
        dto.setGradingSystem(GradingSystem.V_SCALE); dto.setDifficultyGrade("invalid");
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> service.createClimb(dto,10L)).getStatusCode().value());
        verify(climbs, never()).save(any());
    }

    @Test void ownerCanDeleteClimb() throws Exception {
        Climb climb = new Climb(); climb.setUser(user(10L));
        when(climbs.findById(5L)).thenReturn(Optional.of(climb));
        service.deleteClimb(5L,10L);
        verify(climbs).delete(climb);
    }

    @Test void otherUserCannotDeleteClimb() throws Exception {
        Climb climb = new Climb(); climb.setUser(user(10L));
        when(climbs.findById(5L)).thenReturn(Optional.of(climb));
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> service.deleteClimb(5L,20L)).getStatusCode().value());
        verify(climbs, never()).delete(any());
    }

    @Test void deletingMissingClimbReturns404() {
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.deleteClimb(999L,10L)).getStatusCode().value());
        verify(climbs, never()).delete(any());
    }

    @Test void userClimbPaginationMapsResults() throws Exception {
        Climb climb = new Climb(); climb.setUser(user(10L)); climb.setDifficultyGrade("V3");
        climb.setGradingSystem(GradingSystem.V_SCALE); climb.setDate(LocalDate.of(2026,11,1));
        PageRequest page = PageRequest.of(0, 10);
        when(users.existsById(10L)).thenReturn(true);
        when(climbs.findByUserId(10L,page)).thenReturn(new PageImpl<>(List.of(climb),page,1));
        var result = service.getClimbsByUserId(10L,page);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }
}
