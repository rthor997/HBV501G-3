package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.EditWorkoutRequestDTO;
import org.example.klifurapp.DTO.requests.LogWorkoutRequestDTO;
import org.example.klifurapp.DTO.requests.WorkoutResponseDTO;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.entity.Workout;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.repository.WorkoutRepository;
import org.example.klifurapp.exception.ResourceNotFoundException;
import org.example.klifurapp.exception.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceImplementationTest {

    @Mock WorkoutRepository workoutRepository;
    @Mock UserRepository userRepository;
    @InjectMocks WorkoutServiceImplementation service;

    @Test
    void logWorkoutAssociatesAuthenticatedUserAndSavesFields() {
        User owner = user(10L);
        LogWorkoutRequestDTO dto = new LogWorkoutRequestDTO();
        dto.setExerciseName("Pull-ups");
        dto.setSets(3);
        dto.setRepetitions(8);
        dto.setWeight(0.0);
        dto.setDurationMinutes(15);
        when(userRepository.findById(10L)).thenReturn(Optional.of(owner));
        when(workoutRepository.save(any(Workout.class))).thenAnswer(inv -> inv.getArgument(0));

        Workout result = service.logWorkout(dto, 10L);

        assertSame(owner, result.getUser());
        assertEquals("Pull-ups", result.getExerciseName());
        assertEquals(3, result.getSets());
        assertEquals(8, result.getRepetitions());
        assertEquals(0.0, result.getWeight());
        assertEquals(15, result.getDurationMinutes());
        verify(workoutRepository).save(result);
    }

    @Test
    void logWorkoutRejectsMissingUserWithoutSaving() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.logWorkout(new LogWorkoutRequestDTO(), 99L));
        verify(workoutRepository, never()).save(any(Workout.class));
    }

    @Test
    void ownerCanDeleteWorkout() {
        Workout workout = workout(5L, user(10L));
        when(workoutRepository.findById(5L)).thenReturn(Optional.of(workout));

        service.deleteWorkout(5L, 10L);

        verify(workoutRepository).delete(workout);
    }

    @Test
    void nonOwnerCannotDeleteWorkout() {
        Workout workout = workout(5L, user(10L));
        when(workoutRepository.findById(5L)).thenReturn(Optional.of(workout));

        ForbiddenException error = assertThrows(ForbiddenException.class,
                () -> service.deleteWorkout(5L, 20L));

        assertEquals("You do not own this workout", error.getMessage());
        verify(workoutRepository, never()).delete(any(Workout.class));
    }

    @Test
    void deleteMissingWorkoutDoesNotDelete() {
        when(workoutRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteWorkout(99L, 10L));
        verify(workoutRepository, never()).delete(any(Workout.class));
    }

    @Test
    void ownerCanUpdateWorkout() {
        Workout workout = workout(5L, user(10L));
        EditWorkoutRequestDTO dto = new EditWorkoutRequestDTO();
        dto.setExerciseName("Deadlift");
        dto.setSets(4);
        dto.setRepetitions(5);
        dto.setWeight(70.0);
        dto.setDurationMinutes(20);
        when(workoutRepository.findById(5L)).thenReturn(Optional.of(workout));
        when(workoutRepository.save(workout)).thenReturn(workout);

        Workout updated = service.updateWorkout(5L, dto, 10L);

        assertEquals("Deadlift", updated.getExerciseName());
        assertEquals(4, updated.getSets());
        assertEquals(5, updated.getRepetitions());
        assertEquals(70.0, updated.getWeight());
        assertEquals(20, updated.getDurationMinutes());
        verify(workoutRepository).save(workout);
    }

    @Test
    void nonOwnerCannotUpdateWorkout() {
        Workout workout = workout(5L, user(10L));
        when(workoutRepository.findById(5L)).thenReturn(Optional.of(workout));
        EditWorkoutRequestDTO dto = new EditWorkoutRequestDTO();
        dto.setExerciseName("Deadlift");

        assertThrows(ForbiddenException.class,
                () -> service.updateWorkout(5L, dto, 20L));
        verify(workoutRepository, never()).save(any(Workout.class));
        assertEquals("Squat", workout.getExerciseName());
    }

    @Test
    void updateMissingWorkoutDoesNotSave() {
        when(workoutRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateWorkout(99L, new EditWorkoutRequestDTO(), 10L));
        verify(workoutRepository, never()).save(any(Workout.class));
    }

    @Test
    void getWorkoutsReturnsMappedPaginatedDtos() {
        Pageable pageable = PageRequest.of(0, 2);
        Workout first = workout(5L, user(10L));
        Workout second = workout(6L, user(10L));
        second.setExerciseName("Pull-ups");
        when(userRepository.existsById(10L)).thenReturn(true);
        when(workoutRepository.findByUserId(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(first, second), pageable, 3));

        Page<WorkoutResponseDTO> result = service.getWorkoutsByUserId(10L, pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals("Squat", result.getContent().get(0).exerciseName());
        assertEquals("Pull-ups", result.getContent().get(1).exerciseName());
        assertEquals(5L, result.getContent().get(0).id());
    }

    @Test
    void getWorkoutsForMissingUserReturns404() {
        when(userRepository.existsById(99L)).thenReturn(false);

        ResourceNotFoundException error = assertThrows(ResourceNotFoundException.class,
                () -> service.getWorkoutsByUserId(99L, PageRequest.of(0, 10)));

        assertEquals("User not found", error.getMessage());
        verify(workoutRepository, never()).findByUserId(anyLong(), any(Pageable.class));
    }

    private static User user(Long id) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static Workout workout(Long id, User owner) {
        Workout workout = new Workout();
        workout.setId(id);
        workout.setUser(owner);
        workout.setExerciseName("Squat");
        workout.setSets(3);
        workout.setRepetitions(10);
        workout.setWeight(40.0);
        workout.setDurationMinutes(20);
        return workout;
    }
}
