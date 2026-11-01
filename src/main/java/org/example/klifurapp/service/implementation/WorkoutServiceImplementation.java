package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.workout.LogWorkoutRequestDTO;
import org.example.klifurapp.DTO.workout.WorkoutResponseDTO;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.entity.Workout;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.repository.WorkoutRepository;
import org.example.klifurapp.service.WorkoutService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.example.klifurapp.DTO.workout.EditWorkoutRequestDTO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class WorkoutServiceImplementation implements WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;

    public WorkoutServiceImplementation(WorkoutRepository workoutRepository, UserRepository userRepository) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Workout logWorkout(LogWorkoutRequestDTO dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Workout workout = new Workout();

        workout.setUser(user);
        workout.setExerciseName(dto.getExerciseName());
        workout.setSets(dto.getSets());
        workout.setRepetitions(dto.getRepetitions());
        workout.setWeight(dto.getWeight());
        workout.setDurationMinutes(dto.getDurationMinutes());

        return workoutRepository.save(workout);
    }

    @Override
    public boolean deleteWorkout(Long id, Long userId) {
       Workout workout = workoutRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Workout not found"));

        if (!workout.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You do not own this workout");
        }
        workoutRepository.delete(workout);

        return true;
    }

    @Override
    public Workout updateWorkout(Long id, EditWorkoutRequestDTO dto, Long userId) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workout not found"));

        if (!workout.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You do not own this workout"
            );
        }

        workout.setExerciseName(dto.getExerciseName());
        workout.setSets(dto.getSets());
        workout.setRepetitions(dto.getRepetitions());
        workout.setWeight(dto.getWeight());
        workout.setDurationMinutes(dto.getDurationMinutes());

        return workoutRepository.save(workout);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkoutResponseDTO> getWorkoutsByUserId(
            Long userId, Pageable pageable) {

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }

        return workoutRepository.findByUserId(userId, pageable)
                .map(workout -> new WorkoutResponseDTO(
                        workout.getId(),
                        workout.getExerciseName(),
                        workout.getSets(),
                        workout.getRepetitions(),
                        workout.getWeight(),
                        workout.getDurationMinutes()
                ));
    }
}
