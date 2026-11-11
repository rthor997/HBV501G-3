package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.CreateColourGradePresetRequestDTO;
import org.example.klifurapp.DTO.requests.CreateColourGradeRequestDTO;
import org.example.klifurapp.DTO.responses.ColourGradePresetResponseDTO;
import org.example.klifurapp.DTO.responses.ColourGradeResponseDTO;
import org.example.klifurapp.entity.ColourGrade;
import org.example.klifurapp.entity.ColourGradePreset;
import org.example.klifurapp.entity.GradingSystem;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.exception.ConflictException;
import org.example.klifurapp.exception.ForbiddenException;
import org.example.klifurapp.exception.ResourceNotFoundException;
import org.example.klifurapp.repository.ColourGradePresetRepository;
import org.example.klifurapp.repository.ColourGradeRepository;
import org.example.klifurapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColourGradeServiceImplementationTest {

    @Mock ColourGradePresetRepository presetRepository;
    @Mock ColourGradeRepository colourGradeRepository;
    @Mock UserRepository userRepository;
    @InjectMocks ColourGradeServiceImplementation service;

    @Test
    void createPresetIsOwnedByUser() {
        User owner = user(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(owner));
        when(presetRepository.save(any(ColourGradePreset.class))).thenAnswer(invocation -> {
            ColourGradePreset preset = invocation.getArgument(0);
            ReflectionTestUtils.setField(preset, "id", 3L);
            return preset;
        });

        ColourGradePresetResponseDTO result = service.createPreset(
                new CreateColourGradePresetRequestDTO("  Mitt gym "), 10L);

        ArgumentCaptor<ColourGradePreset> captor = ArgumentCaptor.forClass(ColourGradePreset.class);
        verify(presetRepository).save(captor.capture());
        assertSame(owner, captor.getValue().getOwner());
        assertFalse(captor.getValue().isBuiltIn());
        assertEquals(3L, result.id());
        assertEquals("Mitt gym", result.name());
        assertTrue(result.colourGrades().isEmpty());
    }

    @Test
    void createPresetWithDuplicateNameThrowsConflict() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L)));
        when(presetRepository.existsByOwnerIdAndNameIgnoreCase(10L, "Mitt gym")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.createPreset(new CreateColourGradePresetRequestDTO("Mitt gym"), 10L));
        verify(presetRepository, never()).save(any());
    }

    @Test
    void ownerCanCreateColourGrade() {
        ColourGradePreset preset = ownPreset(3L, user(10L));
        when(presetRepository.findById(3L)).thenReturn(Optional.of(preset));
        when(colourGradeRepository.save(any(ColourGrade.class))).thenAnswer(invocation -> {
            ColourGrade grade = invocation.getArgument(0);
            ReflectionTestUtils.setField(grade, "id", 7L);
            return grade;
        });

        ColourGradeResponseDTO result = service.createColourGrade(
                3L, grade(" Blue ", GradingSystem.V_SCALE, "V2", "V4"), 10L);

        assertEquals(7L, result.id());
        assertEquals(3L, result.presetId());
        assertEquals("Blue", result.colour());
        assertEquals(GradingSystem.V_SCALE, result.gradingSystem());
        assertEquals("V2", result.minGrade());
        assertEquals("V4", result.maxGrade());
    }

    @Test
    void fontRangeAndSingleGradeAreAccepted() {
        when(presetRepository.findById(3L)).thenReturn(Optional.of(ownPreset(3L, user(10L))));
        when(colourGradeRepository.save(any(ColourGrade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ColourGradeResponseDTO result = service.createColourGrade(
                3L, grade("Red", GradingSystem.FONT, "6A", "6A"), 10L);

        assertEquals("6A", result.minGrade());
        assertEquals("6A", result.maxGrade());
    }

    @Test
    void builtInPresetCannotBeModified() {
        when(presetRepository.findById(1L)).thenReturn(Optional.of(builtInPreset(1L)));

        assertThrows(ForbiddenException.class, () -> service.createColourGrade(
                1L, grade("Pink", GradingSystem.V_SCALE, "V2", "V4"), 10L));
        verify(colourGradeRepository, never()).save(any());
    }

    @Test
    void otherUsersPresetIsForbidden() {
        when(presetRepository.findById(3L)).thenReturn(Optional.of(ownPreset(3L, user(20L))));

        assertThrows(ForbiddenException.class, () -> service.createColourGrade(
                3L, grade("Blue", GradingSystem.V_SCALE, "V2", "V4"), 10L));
        assertThrows(ForbiddenException.class, () -> service.getColourGrades(3L, 10L));
        verify(colourGradeRepository, never()).save(any());
    }

    @Test
    void missingPresetThrowsNotFound() {
        when(presetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createColourGrade(
                99L, grade("Blue", GradingSystem.V_SCALE, "V2", "V4"), 10L));
    }

    @Test
    void invalidGradeThrowsBadRequest() {
        when(presetRepository.findById(3L)).thenReturn(Optional.of(ownPreset(3L, user(10L))));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createColourGrade(3L, grade("Blue", GradingSystem.V_SCALE, "6A", "V4"), 10L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(colourGradeRepository, never()).save(any());
    }

    @Test
    void reversedRangeThrowsBadRequest() {
        when(presetRepository.findById(3L)).thenReturn(Optional.of(ownPreset(3L, user(10L))));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createColourGrade(3L, grade("Blue", GradingSystem.FONT, "7A", "6C"), 10L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void colourGradingSystemCannotBeUsedForRange() {
        when(presetRepository.findById(3L)).thenReturn(Optional.of(ownPreset(3L, user(10L))));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createColourGrade(3L, grade("Blue", GradingSystem.COLOUR, "V1", "V2"), 10L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void duplicateColourInPresetThrowsConflict() {
        when(presetRepository.findById(3L)).thenReturn(Optional.of(ownPreset(3L, user(10L))));
        when(colourGradeRepository.existsByPresetIdAndColourIgnoreCase(3L, "Blue")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createColourGrade(
                3L, grade("Blue", GradingSystem.V_SCALE, "V2", "V4"), 10L));
        verify(colourGradeRepository, never()).save(any());
    }

    @Test
    void anyUserCanReadBuiltInPreset() {
        ColourGradePreset preset = builtInPreset(1L);
        ColourGrade white = new ColourGrade();
        white.setPreset(preset);
        white.setColour("White");
        white.setGradingSystem(GradingSystem.V_SCALE);
        white.setMinGrade("V0");
        white.setMaxGrade("V0");
        when(presetRepository.findById(1L)).thenReturn(Optional.of(preset));
        when(colourGradeRepository.findByPresetIdOrderByIdAsc(1L)).thenReturn(List.of(white));

        List<ColourGradeResponseDTO> result = service.getColourGrades(1L, 10L);

        assertEquals(1, result.size());
        assertEquals("White", result.get(0).colour());
    }

    @Test
    void selectPresetIsStoredOnUser() {
        User user = user(10L);
        ColourGradePreset preset = builtInPreset(1L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(presetRepository.findById(1L)).thenReturn(Optional.of(preset));

        ColourGradePresetResponseDTO result = service.selectPreset(1L, 10L);

        assertSame(preset, user.getSelectedColourPreset());
        verify(userRepository).save(user);
        assertEquals(1L, result.id());
    }

    @Test
    void cannotSelectOtherUsersPreset() {
        User user = user(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(presetRepository.findById(3L)).thenReturn(Optional.of(ownPreset(3L, user(20L))));

        assertThrows(ForbiddenException.class, () -> service.selectPreset(3L, 10L));
        assertNull(user.getSelectedColourPreset());
    }

    @Test
    void selectedPresetIsEmptyWhenNothingSelected() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L)));

        assertTrue(service.getSelectedPreset(10L).isEmpty());
    }

    @Test
    void usingColourGradeSelectsItsPreset() {
        User user = user(10L);
        ColourGradePreset preset = builtInPreset(1L);
        ColourGrade blue = new ColourGrade();
        blue.setPreset(preset);
        blue.setColour("Blue");
        when(colourGradeRepository.findById(5L)).thenReturn(Optional.of(blue));

        ColourGrade result = service.useColourGrade(5L, user);

        assertSame(blue, result);
        assertSame(preset, user.getSelectedColourPreset());
        verify(userRepository).save(user);
    }

    @Test
    void usingOtherUsersColourGradeIsForbidden() {
        User user = user(10L);
        ColourGrade blue = new ColourGrade();
        blue.setPreset(ownPreset(3L, user(20L)));
        when(colourGradeRepository.findById(5L)).thenReturn(Optional.of(blue));

        assertThrows(ForbiddenException.class, () -> service.useColourGrade(5L, user));
        verify(userRepository, never()).save(any());
    }

    @Test
    void usingMissingColourGradeThrowsNotFound() {
        when(colourGradeRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.useColourGrade(5L, user(10L)));
    }

    private CreateColourGradeRequestDTO grade(String colour, GradingSystem system, String min, String max) {
        return new CreateColourGradeRequestDTO(colour, system, min, max);
    }

    private ColourGradePreset builtInPreset(Long id) {
        ColourGradePreset preset = new ColourGradePreset();
        ReflectionTestUtils.setField(preset, "id", id);
        preset.setName("Klifurhúsið");
        preset.setBuiltIn(true);
        return preset;
    }

    private ColourGradePreset ownPreset(Long id, User owner) {
        ColourGradePreset preset = new ColourGradePreset();
        ReflectionTestUtils.setField(preset, "id", id);
        preset.setName("Mitt gym");
        preset.setOwner(owner);
        return preset;
    }

    private User user(Long id) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        user.setUsername("user" + id);
        return user;
    }
}
