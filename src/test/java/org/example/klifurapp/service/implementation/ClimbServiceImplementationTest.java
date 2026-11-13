package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.CreateClimbRequestDTO;
import org.example.klifurapp.DTO.requests.UpdateClimbRequestDTO;
import org.example.klifurapp.DTO.responses.ClimbResponseDTO;
import org.example.klifurapp.entity.*;
import org.example.klifurapp.exception.ForbiddenException;
import org.example.klifurapp.exception.ResourceNotFoundException;
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
import org.example.klifurapp.repository.RouteRepository;

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

    @Test
    void ownerCanUpdateClimb() throws Exception {
        User owner = user(10L);

        Climb climb = new Climb();
        climb.setUser(owner);
        climb.setGradingSystem(GradingSystem.V_SCALE);
        climb.setDifficultyGrade("V3");
        climb.setClimbingStyle(ClimbingStyle.BOULDERING);
        climb.setDate(LocalDate.of(2026, 11, 1));
        climb.setNotes("Old notes");
        climb.setWallIncline(WallIncline.OVERHANG);

        Field idField = Climb.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(climb, 5L);

        when(climbs.findById(5L))
                .thenReturn(Optional.of(climb));

        when(climbs.save(climb))
                .thenReturn(climb);

        UpdateClimbRequestDTO dto =
                new UpdateClimbRequestDTO(
                        null,
                        "V5",
                        null,
                        null,
                        "Updated notes",
                        null
                );

        ClimbResponseDTO result =
                service.updateClimb(5L, 10L, dto);

        assertEquals("V5", result.difficultyGrade());
        assertEquals("Updated notes", result.notes());

        verify(climbs).save(climb);
    }

    @Test
    void partialUpdatePreservesUnspecifiedFields() throws Exception {
        User owner = user(10L);
        Climb climb = new Climb();
        climb.setUser(owner);
        climb.setGradingSystem(GradingSystem.V_SCALE);
        climb.setDifficultyGrade("V3");
        climb.setClimbingStyle(ClimbingStyle.BOULDERING);
        climb.setDate(LocalDate.of(2026, 11, 1));
        climb.setNotes("Old notes");
        climb.setWallIncline(WallIncline.OVERHANG);
        when(climbs.findById(5L)).thenReturn(Optional.of(climb));
        when(climbs.save(climb)).thenReturn(climb);
        UpdateClimbRequestDTO dto =
                new UpdateClimbRequestDTO(null, null, null,
                        null, "New notes", null);
        ClimbResponseDTO result = service.updateClimb(5L, 10L, dto);
        assertEquals("V3", result.difficultyGrade());
        assertEquals(GradingSystem.V_SCALE, result.gradingSystem());
        assertEquals(ClimbingStyle.BOULDERING, result.climbingStyle());
        assertEquals(LocalDate.of(2026, 11, 1), result.date());
        assertEquals(WallIncline.OVERHANG, result.wallIncline());
        assertEquals("New notes", result.notes());
    }

    @Test
    void otherUserCannotUpdateClimb() throws Exception {
        Climb climb = new Climb();
        climb.setUser(user(10L));
        when(climbs.findById(5L)).thenReturn(Optional.of(climb));
        UpdateClimbRequestDTO dto =
                new UpdateClimbRequestDTO(null, null,
                        null, null, "Trying to edit", null);
        assertThrows(ForbiddenException.class, () -> service.updateClimb(5L, 20L, dto));
        verify(climbs, never()).save(any());
    }


    @Test
    void updatingMissingClimbThrowsResourceNotFoundException() {
        when(climbs.findById(999L)).thenReturn(Optional.empty());
        UpdateClimbRequestDTO dto =
                new UpdateClimbRequestDTO(null, null,
                        null, null, "New notes", null);
        assertThrows(ResourceNotFoundException.class, () -> service.updateClimb(999L, 10L, dto));
        verify(climbs, never()).save(any());
    }

    @Test
    void updateWithInvalidGradeIsRejectedAndNotSaved() throws Exception {
        Climb climb = new Climb();
        climb.setUser(user(10L));
        climb.setGradingSystem(GradingSystem.V_SCALE);
        climb.setDifficultyGrade("V3");
        when(climbs.findById(5L)).thenReturn(Optional.of(climb));
        UpdateClimbRequestDTO dto =
                new UpdateClimbRequestDTO(null, "invalid",
                        null, null, null, null);
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateClimb(5L, 10L, dto)
        );
        assertEquals(400, exception.getStatusCode().value());
        verify(climbs, never()).save(any());
    }




}
