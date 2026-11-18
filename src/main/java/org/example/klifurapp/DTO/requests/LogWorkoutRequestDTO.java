package org.example.klifurapp.DTO.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class LogWorkoutRequestDTO {

    @NotBlank(message = "Exercise name is required")
    @Size(max = 100, message = "Exercise name must be at most 100 characters")
    private String exerciseName;

    @Min(value = 1, message = "Sets must be at least 1")
    private Integer sets;

    @Min(value = 1, message = "Repetitions must be at least 1")
    private Integer repetitions;

    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    private Double weight;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    private Set<Long> muscleGroupIds;

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public Integer getSets() {
        return sets;
    }

    public void setSets(Integer sets) {
        this.sets = sets;
    }

    public Integer getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(Integer repetitions) {
        this.repetitions = repetitions;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Set<Long> getMuscleGroupIds() {
        return muscleGroupIds;
    }

    public void setMuscleGroupIds(Set<Long> muscleGroupIds) {
        this.muscleGroupIds = muscleGroupIds;
    }
}
