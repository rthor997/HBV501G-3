package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.EditWorkoutRequestDTO;
import org.example.klifurapp.DTO.requests.LogWorkoutRequestDTO;
import org.example.klifurapp.DTO.requests.WorkoutResponseDTO;
import org.example.klifurapp.entity.MuscleGroup;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.entity.Workout;
import org.example.klifurapp.exception.ForbiddenException;
import org.example.klifurapp.exception.ResourceNotFoundException;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.repository.WorkoutRepository;
import org.example.klifurapp.service.MuscleGroupService;
import org.example.klifurapp.service.WorkoutService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

@Service
public class WorkoutServiceImplementation implements WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final MuscleGroupService muscleGroupService;

    public WorkoutServiceImplementation(
            WorkoutRepository workoutRepository,
            UserRepository userRepository,
            MuscleGroupService muscleGroupService) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
        this.muscleGroupService = muscleGroupService;
    }

    @Override
    public Workout logWorkout(LogWorkoutRequestDTO dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Workout workout = new Workout();

        workout.setUser(user);
        workout.setExerciseName(dto.getExerciseName());
        workout.setSets(dto.getSets());
        workout.setRepetitions(dto.getRepetitions());
        workout.setWeight(dto.getWeight());
        workout.setDurationMinutes(dto.getDurationMinutes());
        workout.setMuscleGroups(resolveMuscleGroups(dto.getMuscleGroupIds()));

        return workoutRepository.save(workout);
    }

    @Override
    public void deleteWorkout(Long id, Long userId) {
       Workout workout = workoutRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Workout not found"));

        if (!workout.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this workout");
        }
        workoutRepository.delete(workout);
    }

    @Override
    public Workout updateWorkout(Long id, EditWorkoutRequestDTO dto, Long userId) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found"));

        if (!workout.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this workout");
        }

        workout.setExerciseName(dto.getExerciseName());
        workout.setSets(dto.getSets());
        workout.setRepetitions(dto.getRepetitions());
        workout.setWeight(dto.getWeight());
        workout.setDurationMinutes(dto.getDurationMinutes());

        if (dto.getMuscleGroupIds() != null) {
            workout.setMuscleGroups(resolveMuscleGroups(dto.getMuscleGroupIds()));
        }

        return workoutRepository.save(workout);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkoutResponseDTO> getWorkoutsByUserId(
            Long userId, Pageable pageable) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return workoutRepository.findByUserId(userId, pageable)
                .map(workout -> new WorkoutResponseDTO(
                        workout.getId(),
                        workout.getExerciseName(),
                        workout.getSets(),
                        workout.getRepetitions(),
                        workout.getWeight(),
                        workout.getDurationMinutes(),
                        workout.getMuscleGroups().stream()
                                .map(MuscleGroup::getName)
                                .sorted()
                                .toList()
                ));
    }

    private Set<MuscleGroup> resolveMuscleGroups(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return muscleGroupService.findAllByIds(ids);
    }
}
