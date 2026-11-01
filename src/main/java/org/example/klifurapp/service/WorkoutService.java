package org.example.klifurapp.service;

import org.example.klifurapp.DTO.requests.EditWorkoutRequestDTO;
import org.example.klifurapp.DTO.requests.LogWorkoutRequestDTO;
import org.example.klifurapp.DTO.requests.WorkoutResponseDTO;
import org.example.klifurapp.entity.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkoutService {
    Workout logWorkout(LogWorkoutRequestDTO dto, Long userId);
    void deleteWorkout(Long id, Long userId);
    Workout updateWorkout(Long id, EditWorkoutRequestDTO dto, Long userId);
    Page<WorkoutResponseDTO> getWorkoutsByUserId(Long userId, Pageable pageable);
}
