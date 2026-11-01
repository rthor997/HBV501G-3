package org.example.klifurapp.DTO.climb;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.klifurapp.entity.ClimbingStyle;
import org.example.klifurapp.entity.GradingSystem;
import org.example.klifurapp.entity.WallIncline;

import java.time.LocalDate;

public class CreateClimbDTO {

    @NotNull
    private GradingSystem gradingSystem;

    @NotBlank
    private String difficultyGrade;

    @NotNull
    private ClimbingStyle climbingStyle;

    @NotNull
    private LocalDate date;

    private String notes;

    private WallIncline wallIncline;

    public GradingSystem getGradingSystem() {
        return gradingSystem;
    }

    public void setGradingSystem(GradingSystem gradingSystem) {
        this.gradingSystem = gradingSystem;
    }

    public String getDifficultyGrade() {
        return difficultyGrade;
    }

    public void setDifficultyGrade(String difficultyGrade) {
        this.difficultyGrade = difficultyGrade;
    }

    public ClimbingStyle getClimbingStyle() {
        return climbingStyle;
    }

    public void setClimbingStyle(ClimbingStyle climbingStyle) {
        this.climbingStyle = climbingStyle;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public WallIncline getWallIncline() {
        return wallIncline;
    }

    public void setWallIncline(WallIncline wallIncline) {
        this.wallIncline = wallIncline;
    }
}