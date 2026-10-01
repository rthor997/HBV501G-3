package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.workout.LogWorkoutRequestDTO;
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
    public Workout logWorkout(LogWorkoutRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
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
}
