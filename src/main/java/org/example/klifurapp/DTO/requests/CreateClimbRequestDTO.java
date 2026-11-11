package org.example.klifurapp.DTO.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import org.example.klifurapp.entity.ClimbingStyle;
import org.example.klifurapp.entity.GradingSystem;
import org.example.klifurapp.entity.WallIncline;

import java.time.LocalDate;

public class CreateClimbRequestDTO {

    @NotNull
    private GradingSystem gradingSystem;

    private String difficultyGrade;

    private Long colourGradeId;

    @NotNull
    private ClimbingStyle climbingStyle;

    @NotNull
    private LocalDate date;

    private String notes;

    private WallIncline wallIncline;

    private Long routeId;

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

    public Long getRouteId() {
        return routeId;
    }

    public Long getColourGradeId() {
        return colourGradeId;
    }

    public void setColourGradeId(Long colourGradeId) {
        this.colourGradeId = colourGradeId;
    }

    @JsonIgnore
    @AssertTrue(message = "difficultyGrade is required, or colourGradeId when gradingSystem is COLOUR")
    public boolean isGradeProvided() {
        if (gradingSystem == GradingSystem.COLOUR) {
            return colourGradeId != null;
        }
        return difficultyGrade != null && !difficultyGrade.isBlank();
    }
}