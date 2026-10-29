package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.LogWorkoutRequestDTO;
import org.example.klifurapp.DTO.requests.EditWorkoutRequestDTO;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.entity.Workout;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.repository.WorkoutRepository;
import org.example.klifurapp.service.WorkoutService;
import org.springframework.stereotype.Service;

@Service
public class WorkoutServiceImplementation implements WorkoutService {
    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;

    public WorkoutServiceImplementation(WorkoutRepository workoutRepository, UserRepository userRepository) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Workout logWorkout(String userEmail, LogWorkoutRequestDTO dto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Notandi fannst ekki"));

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
    public boolean deleteWorkout(Long id) {
        if (!workoutRepository.existsById(id)) {
            throw new IllegalArgumentException("Workout not found");
        }
        workoutRepository.deleteById(id);
        return !workoutRepository.existsById(id);
    }

    @Override
    public Workout updateWorkout(Long id, EditWorkoutRequestDTO dto) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Æfing fannst ekki"));

        workout.setExerciseName(dto.getExerciseName());
        workout.setSets(dto.getSets());
        workout.setRepetitions(dto.getRepetitions());
        workout.setWeight(dto.getWeight());
        workout.setDurationMinutes(dto.getDurationMinutes());

        return workoutRepository.save(workout);
    }
}