package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.AdminUpdateClimbDTO;
import org.example.klifurapp.DTO.requests.CreateClimbRequestDTO;
import org.example.klifurapp.DTO.requests.UpdateClimbRequestDTO;
import org.example.klifurapp.DTO.responses.ClimbResponseDTO;
import org.example.klifurapp.entity.*;
import org.example.klifurapp.exception.BadRequestException;
import org.example.klifurapp.exception.ForbiddenException;
import org.example.klifurapp.exception.ResourceNotFoundException;
import org.example.klifurapp.repository.ClimbRepository;
import org.example.klifurapp.repository.RouteRepository;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.ColourGradeService;
import org.example.klifurapp.strategy.NewestFirstStrategy;
import org.example.klifurapp.strategy.OldestFirstStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
    ColourGradeService colourGrades = mock(ColourGradeService.class);
    @BeforeEach
    void setup() {
        service = new ClimbServiceImplementation(
                climbs,
                users,
                routes,
                colourGrades,
                List.of(
                        new NewestFirstStrategy(),
                        new OldestFirstStrategy()
                )
        );
    }

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
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.createClimb(dto, 10L)
        );
        assertEquals("Invalid difficulty grade for V_SCALE", exception.getMessage());
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
        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> service.deleteClimb(5L, 20L)
        );
        assertEquals("You do not own this climb",exception.getMessage());
        verify(climbs, never()).delete(any());
    }

    @Test void deletingMissingClimbReturns404() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.deleteClimb(999L,10L)
        );
        assertEquals("Climb not found", exception.getMessage() );
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

    @Test void colourClimbUsesColourGradeAsDifficulty() throws Exception {
        User owner = user(10L);
        ColourGrade blue = new ColourGrade(); blue.setColour("Blue");
        when(users.findById(10L)).thenReturn(Optional.of(owner));
        when(colourGrades.useColourGrade(5L, owner)).thenReturn(blue);
        when(climbs.save(any(Climb.class))).thenAnswer(inv -> inv.getArgument(0));
        CreateClimbRequestDTO dto = new CreateClimbRequestDTO();
        dto.setGradingSystem(GradingSystem.COLOUR); dto.setColourGradeId(5L);
        dto.setClimbingStyle(ClimbingStyle.BOULDERING); dto.setDate(LocalDate.of(2026,11,1));
        Climb result = service.createClimb(dto, 10L);
        assertEquals("Blue", result.getDifficultyGrade());
        assertSame(blue, result.getColourGrade());
    }

    @Test void colourClimbWithoutColourGradeIdIsRejected() throws Exception {
        when(users.findById(10L)).thenReturn(Optional.of(user(10L)));
        CreateClimbRequestDTO dto = new CreateClimbRequestDTO();
        dto.setGradingSystem(GradingSystem.COLOUR); dto.setDifficultyGrade("Blue");
        assertThrows(BadRequestException.class, () -> service.createClimb(dto,10L));
        verifyNoInteractions(colourGrades);
        verify(climbs, never()).save(any());
    }

    @Test void colourGradeIdWithFontIsRejected() throws Exception {
        when(users.findById(10L)).thenReturn(Optional.of(user(10L)));
        CreateClimbRequestDTO dto = new CreateClimbRequestDTO();
        dto.setGradingSystem(GradingSystem.FONT); dto.setDifficultyGrade("6A"); dto.setColourGradeId(5L);
        assertThrows(BadRequestException.class, () -> service.createClimb(dto,10L));
        verifyNoInteractions(colourGrades);
        verify(climbs, never()).save(any());
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
                , null);

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
                        null, "New notes", null, null);
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
                        null, null, "Trying to edit", null, null);
        assertThrows(ForbiddenException.class, () -> service.updateClimb(5L, 20L, dto));
        verify(climbs, never()).save(any());
    }


    @Test
    void updatingMissingClimbThrowsResourceNotFoundException() {
        when(climbs.findById(999L)).thenReturn(Optional.empty());
        UpdateClimbRequestDTO dto =
                new UpdateClimbRequestDTO(null, null,
                        null, null, "New notes", null, null);
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
                        null, null, null, null, null);
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.updateClimb(5L, 10L, dto)
        );
        assertEquals("Invalid difficulty grade for V_SCALE", exception.getMessage());
        verify(climbs, never()).save(any());
    }

    private Climb existingClimb(GradingSystem system, String grade, ColourGrade colourGrade) throws Exception {
        Climb climb = new Climb();
        climb.setUser(user(10L));
        climb.setGradingSystem(system);
        climb.setDifficultyGrade(grade);
        climb.setColourGrade(colourGrade);
        climb.setClimbingStyle(ClimbingStyle.BOULDERING);
        climb.setDate(LocalDate.of(2026, 11, 1));
        when(climbs.findById(5L)).thenReturn(Optional.of(climb));
        when(climbs.save(climb)).thenReturn(climb);
        return climb;
    }

    private ColourGrade colour(String name) {
        ColourGrade grade = new ColourGrade();
        grade.setColour(name);
        return grade;
    }

    private UpdateClimbRequestDTO patch(GradingSystem system, String grade, Long colourGradeId, String notes) {
        return new UpdateClimbRequestDTO(system, grade, null, null, notes, null, colourGradeId);
    }

    @Test void switchingFromVScaleToColourUsesColourGrade() throws Exception {
        Climb climb = existingClimb(GradingSystem.V_SCALE, "V3", null);
        ColourGrade blue = colour("Blue");
        when(colourGrades.useColourGrade(7L, climb.getUser())).thenReturn(blue);

        ClimbResponseDTO result = service.updateClimb(5L, 10L, patch(GradingSystem.COLOUR, null, 7L, null));

        assertEquals(GradingSystem.COLOUR, result.gradingSystem());
        assertEquals("Blue", result.difficultyGrade());
        assertSame(blue, climb.getColourGrade());
    }

    @Test void switchingToColourWithoutColourGradeIdIsRejected() throws Exception {
        existingClimb(GradingSystem.V_SCALE, "V3", null);

        assertThrows(BadRequestException.class,
                () -> service.updateClimb(5L, 10L, patch(GradingSystem.COLOUR, null, null, null)));
        verify(climbs, never()).save(any());
    }

    @Test void switchingFromColourToFontClearsColourGrade() throws Exception {
        Climb climb = existingClimb(GradingSystem.COLOUR, "Blue", colour("Blue"));

        ClimbResponseDTO result = service.updateClimb(5L, 10L, patch(GradingSystem.FONT, "6A", null, null));

        assertEquals(GradingSystem.FONT, result.gradingSystem());
        assertEquals("6A", result.difficultyGrade());
        assertNull(climb.getColourGrade());
    }

    @Test void switchingFromColourWithoutDifficultyGradeIsRejected() throws Exception {
        Climb climb = existingClimb(GradingSystem.COLOUR, "Blue", colour("Blue"));

        assertThrows(BadRequestException.class,
                () -> service.updateClimb(5L, 10L, patch(GradingSystem.V_SCALE, null, null, null)));
        assertEquals(GradingSystem.COLOUR, climb.getGradingSystem());
        assertNotNull(climb.getColourGrade());
        verify(climbs, never()).save(any());
    }

    @Test void editingNotesOnColourClimbKeepsColourGrade() throws Exception {
        ColourGrade blue = colour("Blue");
        Climb climb = existingClimb(GradingSystem.COLOUR, "Blue", blue);

        ClimbResponseDTO result = service.updateClimb(5L, 10L, patch(null, null, null, "Flash"));

        assertEquals("Blue", result.difficultyGrade());
        assertEquals("Flash", result.notes());
        assertSame(blue, climb.getColourGrade());
        verifyNoInteractions(colourGrades);
    }

    @Test void changingColourOnColourClimbUsesNewColourGrade() throws Exception {
        Climb climb = existingClimb(GradingSystem.COLOUR, "Blue", colour("Blue"));
        when(colourGrades.useColourGrade(8L, climb.getUser())).thenReturn(colour("Red"));

        ClimbResponseDTO result = service.updateClimb(5L, 10L, patch(null, null, 8L, null));

        assertEquals("Red", result.difficultyGrade());
    }

    @Test void difficultyGradeOnColourClimbIsRejected() throws Exception {
        existingClimb(GradingSystem.COLOUR, "Blue", colour("Blue"));

        assertThrows(BadRequestException.class,
                () -> service.updateClimb(5L, 10L, patch(null, "V4", null, null)));
        verify(climbs, never()).save(any());
    }

    @Test void colourGradeIdOnVScaleClimbIsRejected() throws Exception {
        existingClimb(GradingSystem.V_SCALE, "V3", null);

        assertThrows(BadRequestException.class,
                () -> service.updateClimb(5L, 10L, patch(null, null, 7L, null)));
        verifyNoInteractions(colourGrades);
    }

    @Test void adminColourUpdateDoesNotChangeOwnersSelectedPreset() throws Exception {
        Climb climb = existingClimb(GradingSystem.V_SCALE, "V3", null);
        when(colourGrades.getUsableColourGrade(7L, climb.getUser())).thenReturn(colour("Blue"));

        Climb result = service.updateClimbAsAdmin(5L,
                new AdminUpdateClimbDTO(GradingSystem.COLOUR, null, null, null, null, null, 7L));

        assertEquals("Blue", result.getDifficultyGrade());
        verify(colourGrades, never()).useColourGrade(anyLong(), any());
    }

    @Test
    void getClimbingHistoryUsesOldestSortingStrategy() {

        PageRequest pageable = PageRequest.of(0, 10);

        PageRequest expectedPageable = PageRequest.of(
                0,
                10,
                Sort.by(
                        Sort.Order.asc("date"),
                        Sort.Order.asc("id")
                )
        );

        when(climbs.findFilteredClimbs(
                eq(10L),
                isNull(),
                isNull(),
                isNull(),
                eq(expectedPageable)
        )).thenReturn(new PageImpl<>(List.of()));

        service.getClimbingHistory(
                10L,
                null,
                null,
                null,
                "oldest",
                pageable
        );

        verify(climbs).findFilteredClimbs(
                eq(10L),
                isNull(),
                isNull(),
                isNull(),
                eq(expectedPageable)
        );
    }

    @Test
    void invalidSortingStrategyThrowsBadRequest() {

        PageRequest pageable = PageRequest.of(0, 10);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getClimbingHistory(
                        10L,
                        null,
                        null,
                        null,
                        "invalid",
                        pageable
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(climbs, never()).findFilteredClimbs(
                any(), any(), any(), any(), any()
        );
    }
}