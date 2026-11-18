package org.example.klifurapp.integration;

import org.example.klifurapp.DTO.requests.LogWorkoutRequestDTO;
import org.example.klifurapp.DTO.requests.WorkoutResponseDTO;
import org.example.klifurapp.DTO.responses.MuscleGroupResponseDTO;
import org.example.klifurapp.config.MuscleGroupSeeder;
import org.example.klifurapp.entity.Role;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.exception.BadRequestException;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.MuscleGroupService;
import org.example.klifurapp.service.WorkoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MuscleGroupIntegrationTest {

    @Autowired MuscleGroupService muscleGroupService;
    @Autowired WorkoutService workoutService;
    @Autowired UserRepository userRepository;

    @Test
    void workoutIsSavedWithMuscleGroups() {
        Map<String, Long> ids = muscleGroupService.getAll().stream()
                .collect(Collectors.toMap(MuscleGroupResponseDTO::name, MuscleGroupResponseDTO::id));
        assertTrue(ids.keySet().containsAll(MuscleGroupSeeder.MUSCLE_GROUPS));

        User user = new User();
        user.setUsername("muscle_user");
        user.setEmail("muscle_user@example.com");
        user.setPasswordHash("test-only-hash");
        user.setRole(Role.USER);
        userRepository.save(user);

        LogWorkoutRequestDTO dto = new LogWorkoutRequestDTO();
        dto.setExerciseName("Pull-ups");
        dto.setMuscleGroupIds(Set.of(ids.get("Back"), ids.get("Biceps")));
        workoutService.logWorkout(dto, user.getId());

        WorkoutResponseDTO saved = workoutService
                .getWorkoutsByUserId(user.getId(), PageRequest.of(0, 10))
                .getContent().get(0);
        assertEquals(List.of("Back", "Biceps"), saved.muscleGroups());

        dto.setMuscleGroupIds(Set.of(-1L));
        assertThrows(BadRequestException.class, () -> workoutService.logWorkout(dto, user.getId()));
    }
}
