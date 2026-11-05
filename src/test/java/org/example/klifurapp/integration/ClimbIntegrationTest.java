package org.example.klifurapp.integration;

import org.example.klifurapp.DTO.requests.CreateClimbRequestDTO;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.entity.ClimbingStyle;
import org.example.klifurapp.entity.GradingSystem;
import org.example.klifurapp.entity.Role;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.ClimbRepository;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.ClimbService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClimbIntegrationTest {
    @Autowired ClimbService climbService;
    @Autowired ClimbRepository climbRepository;
    @Autowired UserRepository userRepository;

    private User createUser(String suffix) {
        User user = new User();
        user.setUsername("user_" + suffix);
        user.setEmail(suffix + "@example.com");
        user.setPasswordHash("test-only-hash");
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    private CreateClimbRequestDTO request(String grade, LocalDate date) {
        CreateClimbRequestDTO dto = new CreateClimbRequestDTO();
        ReflectionTestUtils.setField(dto, "gradingSystem", GradingSystem.V_SCALE);
        ReflectionTestUtils.setField(dto, "difficultyGrade", grade);
        ReflectionTestUtils.setField(dto, "climbingStyle", ClimbingStyle.BOULDERING);
        ReflectionTestUtils.setField(dto, "date", date);
        ReflectionTestUtils.setField(dto, "notes", "Integration test climb");
        return dto;
    }

    @Test void createClimbPersistsCorrectOwnerAndFields() {
        User owner = createUser("owner1");
        Climb created = climbService.createClimb(request("V3", LocalDate.of(2026, 11, 1)), owner.getId());
        assertNotNull(created.getId());
        Climb saved = climbRepository.findById(created.getId()).orElseThrow();
        assertEquals(owner.getId(), saved.getUser().getId());
        assertEquals("V3", saved.getDifficultyGrade());
        assertEquals(GradingSystem.V_SCALE, saved.getGradingSystem());
        assertEquals(ClimbingStyle.BOULDERING, saved.getClimbingStyle());
    }

    @Test void invalidGradeIsRejectedWithoutSaving() {
        User owner = createUser("owner2");
        long before = climbRepository.count();
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
            () -> climbService.createClimb(request("INVALID", LocalDate.now()), owner.getId()));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals(before, climbRepository.count());
    }

    @Test void missingUserCannotCreateClimb() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
            () -> climbService.createClimb(request("V2", LocalDate.now()), -999L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test void ownerCanDeleteClimbFromDatabase() {
        User owner = createUser("owner3");
        Climb climb = climbService.createClimb(request("V2", LocalDate.now()), owner.getId());
        climbService.deleteClimb(climb.getId(), owner.getId());
        assertFalse(climbRepository.existsById(climb.getId()));
    }

    @Test void otherUserCannotDeleteClimb() {
        User owner = createUser("owner4");
        User stranger = createUser("stranger4");
        Climb climb = climbService.createClimb(request("V4", LocalDate.now()), owner.getId());
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
            () -> climbService.deleteClimb(climb.getId(), stranger.getId()));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        assertTrue(climbRepository.existsById(climb.getId()));
    }

    @Test void paginatedClimbsContainOnlyRequestedUsersRecords() {
        User owner = createUser("owner5");
        User other = createUser("other5");
        climbService.createClimb(request("V1", LocalDate.of(2026, 10, 1)), owner.getId());
        climbService.createClimb(request("V2", LocalDate.of(2026, 10, 2)), owner.getId());
        climbService.createClimb(request("V3", LocalDate.of(2026, 10, 3)), owner.getId());
        climbService.createClimb(request("V4", LocalDate.of(2026, 10, 4)), other.getId());

        PageRequest first = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "date"));
        Page<?> page0 = climbService.getClimbsByUserId(owner.getId(), first);
        Page<?> page1 = climbService.getClimbsByUserId(owner.getId(), first.next());
        assertEquals(3, page0.getTotalElements());
        assertEquals(2, page0.getTotalPages());
        assertEquals(2, page0.getNumberOfElements());
        assertEquals(1, page1.getNumberOfElements());
    }
}
