package org.example.klifurapp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "difficulty_grade")
    private String difficultyGrade;

    @Column(name = "grading_system")
    private String gradingSystem;

    @Column(name = "climbing_style")
    private String climbingStyle;

    @Column(name = "wall_incline")
    private String wallIncline;

    private String characteristics;

    private Boolean indoor;

    private String location;

    public Route() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDifficultyGrade() {
        return difficultyGrade;
    }

    public String getGradingSystem() {
        return gradingSystem;
    }

    public String getClimbingStyle() {
        return climbingStyle;
    }

    public String getWallIncline() {
        return wallIncline;
    }

    public String getCharacteristics() {
        return characteristics;
    }

    public Boolean getIndoor() {
        return indoor;
    }

    public String getLocation() {
        return location;
    }
}