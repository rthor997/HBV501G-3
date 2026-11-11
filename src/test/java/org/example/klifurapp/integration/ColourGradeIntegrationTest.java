package org.example.klifurapp.integration;

import org.example.klifurapp.DTO.requests.CreateClimbRequestDTO;
import org.example.klifurapp.DTO.requests.CreateColourGradePresetRequestDTO;
import org.example.klifurapp.DTO.requests.CreateColourGradeRequestDTO;
import org.example.klifurapp.DTO.responses.ColourGradePresetResponseDTO;
import org.example.klifurapp.DTO.responses.ColourGradeResponseDTO;
import org.example.klifurapp.config.ColourGradePresetSeeder;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.entity.ClimbingStyle;
import org.example.klifurapp.entity.ColourGrade;
import org.example.klifurapp.entity.GradingSystem;
import org.example.klifurapp.entity.Role;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.exception.ConflictException;
import org.example.klifurapp.exception.ForbiddenException;
import org.example.klifurapp.repository.ClimbRepository;
import org.example.klifurapp.repository.ColourGradePresetRepository;
import org.example.klifurapp.repository.ColourGradeRepository;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.ClimbService;
import org.example.klifurapp.service.ColourGradeService;
import org.example.klifurapp.service.GradeValidator;
import org.example.klifurapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ColourGradeIntegrationTest {

    @Autowired ColourGradeService colourGradeService;
    @Autowired ClimbService climbService;
    @Autowired UserService userService;
    @Autowired ColourGradePresetRepository presetRepository;
    @Autowired ColourGradeRepository colourGradeRepository;
    @Autowired ClimbRepository climbRepository;
    @Autowired UserRepository userRepository;
    @Autowired EntityManager entityManager;

    private User createUser(String suffix) {
        User user = new User();
        user.setUsername("grader_" + suffix);
        user.setEmail("grader_" + suffix + "@example.com");
        user.setPasswordHash("test-only-hash");
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    private ColourGradePresetResponseDTO klifurhusid(Long userId) {
        return colourGradeService.getPresets(userId).stream()
                .filter(ColourGradePresetResponseDTO::builtIn)
                .filter(p -> p.name().equals(ColourGradePresetSeeder.KLIFURHUSID))
                .findFirst()
                .orElseThrow();
    }

    private CreateClimbRequestDTO colourClimb(Long colourGradeId) {
        CreateClimbRequestDTO dto = new CreateClimbRequestDTO();
        dto.setGradingSystem(GradingSystem.COLOUR);
        dto.setColourGradeId(colourGradeId);
        dto.setClimbingStyle(ClimbingStyle.BOULDERING);
        dto.setDate(LocalDate.of(2026, 11, 10));
        return dto;
    }

    @Test
    void klifurhusidIsBuiltInAndVisibleToEveryone() {
        User user = createUser("a");

        ColourGradePresetResponseDTO preset = klifurhusid(user.getId());

        assertEquals(
                List.of("White", "Green", "Blue", "Yellow", "Orange", "Red", "Black", "Silver"),
                preset.colourGrades().stream().map(ColourGradeResponseDTO::colour).toList());
        assertThrows(ForbiddenException.class, () -> colourGradeService.createColourGrade(
                preset.id(),
                new CreateColourGradeRequestDTO("Pink", GradingSystem.V_SCALE, "V1", "V2"),
                user.getId()));
    }

    @Test
    void ownPresetIsOnlyVisibleToOwner() {
        User owner = createUser("b1");
        User other = createUser("b2");

        ColourGradePresetResponseDTO preset = colourGradeService.createPreset(
                new CreateColourGradePresetRequestDTO("Heimaveggur"), owner.getId());
        colourGradeService.createColourGrade(preset.id(),
                new CreateColourGradeRequestDTO("Blue", GradingSystem.V_SCALE, "V2", "V4"), owner.getId());
        colourGradeService.createColourGrade(preset.id(),
                new CreateColourGradeRequestDTO("Red", GradingSystem.FONT, "6B", "7A"), owner.getId());

        assertEquals(List.of("Blue", "Red"), colourGradeService.getColourGrades(preset.id(), owner.getId())
                .stream().map(ColourGradeResponseDTO::colour).toList());
        assertTrue(colourGradeService.getPresets(owner.getId()).stream()
                .anyMatch(p -> p.id().equals(preset.id())));
        assertFalse(colourGradeService.getPresets(other.getId()).stream()
                .anyMatch(p -> p.id().equals(preset.id())));
        assertThrows(ForbiddenException.class,
                () -> colourGradeService.getColourGrades(preset.id(), other.getId()));
    }

    @Test
    void duplicateColourIsRejected() {
        User owner = createUser("c");
        ColourGradePresetResponseDTO preset = colourGradeService.createPreset(
                new CreateColourGradePresetRequestDTO("Gym C"), owner.getId());
        colourGradeService.createColourGrade(preset.id(),
                new CreateColourGradeRequestDTO("Blue", GradingSystem.V_SCALE, "V2", "V4"), owner.getId());

        assertThrows(ConflictException.class, () -> colourGradeService.createColourGrade(preset.id(),
                new CreateColourGradeRequestDTO("blue", GradingSystem.V_SCALE, "V3", "V5"), owner.getId()));
    }

    @Test
    void invalidRangeIsRejectedWithoutSaving() {
        User owner = createUser("d");
        ColourGradePresetResponseDTO preset = colourGradeService.createPreset(
                new CreateColourGradePresetRequestDTO("Gym D"), owner.getId());
        long before = colourGradeRepository.count();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> colourGradeService.createColourGrade(preset.id(),
                        new CreateColourGradeRequestDTO("Black", GradingSystem.V_SCALE, "V8", "V5"), owner.getId()));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(before, colourGradeRepository.count());
    }

    @Test
    void loggingColourClimbStoresColourAndSelectsPreset() {
        User user = createUser("e");
        ColourGradePresetResponseDTO preset = klifurhusid(user.getId());
        ColourGradeResponseDTO blue = preset.colourGrades().stream()
                .filter(g -> g.colour().equals("Blue")).findFirst().orElseThrow();

        assertTrue(colourGradeService.getSelectedPreset(user.getId()).isEmpty());

        Climb climb = climbService.createClimb(colourClimb(blue.id()), user.getId());

        Climb saved = climbRepository.findById(climb.getId()).orElseThrow();
        assertEquals(GradingSystem.COLOUR, saved.getGradingSystem());
        assertEquals("Blue", saved.getDifficultyGrade());
        assertEquals(blue.id(), saved.getColourGradeId());
        assertEquals(preset.id(), colourGradeService.getSelectedPreset(user.getId()).orElseThrow().id());
    }

    @Test
    void cannotLogClimbWithOtherUsersColourGrade() {
        User owner = createUser("f1");
        User other = createUser("f2");
        ColourGradePresetResponseDTO preset = colourGradeService.createPreset(
                new CreateColourGradePresetRequestDTO("Gym F"), owner.getId());
        ColourGradeResponseDTO grade = colourGradeService.createColourGrade(preset.id(),
                new CreateColourGradeRequestDTO("Blue", GradingSystem.V_SCALE, "V2", "V4"), owner.getId());
        long before = climbRepository.count();

        assertThrows(ForbiddenException.class,
                () -> climbService.createClimb(colourClimb(grade.id()), other.getId()));
        assertEquals(before, climbRepository.count());
    }

    @Test
    void colourGradeIdWithVScaleIsRejected() {
        User user = createUser("g");
        CreateClimbRequestDTO dto = colourClimb(1L);
        dto.setGradingSystem(GradingSystem.V_SCALE);
        dto.setDifficultyGrade("V3");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> climbService.createClimb(dto, user.getId()));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void deletingUserRemovesOwnPresetsButNotBuiltIn() {
        User owner = createUser("h");
        ColourGradePresetResponseDTO preset = colourGradeService.createPreset(
                new CreateColourGradePresetRequestDTO("Gym H"), owner.getId());
        ColourGradeResponseDTO grade = colourGradeService.createColourGrade(preset.id(),
                new CreateColourGradeRequestDTO("Blue", GradingSystem.V_SCALE, "V2", "V4"), owner.getId());
        climbService.createClimb(colourClimb(grade.id()), owner.getId());
        entityManager.flush();
        entityManager.clear();

        userService.deleteUser(owner.getId());
        entityManager.flush();
        entityManager.clear();

        assertFalse(presetRepository.existsById(preset.id()));
        assertFalse(colourGradeRepository.existsById(grade.id()));
        assertTrue(presetRepository.existsByBuiltInTrueAndNameIgnoreCase(ColourGradePresetSeeder.KLIFURHUSID));
    }

    @Test
    void seederDoesNotDuplicateBuiltInPreset() throws Exception {
        long before = presetRepository.count();
        new ColourGradePresetSeeder(presetRepository, colourGradeRepository).run(null);
        assertEquals(before, presetRepository.count());
    }

    @Test
    void builtInGradesAreValidRanges() {
        List<ColourGrade> grades = colourGradeRepository.findAll().stream()
                .filter(g -> g.getPreset().isBuiltIn())
                .toList();
        assertFalse(grades.isEmpty());
        for (ColourGrade grade : grades) {
            assertTrue(GradeValidator.compare(grade.getGradingSystem(), grade.getMinGrade(), grade.getMaxGrade()) <= 0,
                    grade.getColour());
        }
    }
}
