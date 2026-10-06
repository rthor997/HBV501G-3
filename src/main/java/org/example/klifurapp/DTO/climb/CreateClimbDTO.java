package org.example.klifurapp.DTO.climb;

import jakarta.validation.constraints.NotNull;
import org.example.klifurapp.entity.ClimbingStyle;
import org.example.klifurapp.entity.DifficultyGrade;
import org.example.klifurapp.entity.WallIncline;

import java.time.LocalDate;

public class CreateClimbDTO {

    @NotNull
    private Long userId;

    @NotNull
    private DifficultyGrade difficultyGrade;

    @NotNull
    private ClimbingStyle climbingStyle;

    @NotNull
    private LocalDate date;

    private String notes;

    private WallIncline wallIncline;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public DifficultyGrade getDifficultyGrade() {
        return difficultyGrade;
    }

    public void setDifficultyGrade(DifficultyGrade difficultyGrade) {
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