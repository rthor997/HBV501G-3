
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GradingSystem gradingSystem;

    @Column(nullable = false)
    private String difficultyGrade;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClimbingStyle climbingStyle;

    @Enumerated(EnumType.STRING)
    private WallIncline wallIncline;

    private boolean indoor;

    private String characteristics;

    public Route() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDifficultyGrade() {
        return difficultyGrade;
    }

    public void setDifficultyGrade(String difficultyGrade) {
        this.difficultyGrade = difficultyGrade;
    }

    public GradingSystem getGradingSystem() {
        return gradingSystem;
    }

    public void setGradingSystem(GradingSystem gradingSystem) {
        this.gradingSystem = gradingSystem;
    }

    public ClimbingStyle getClimbingStyle() {
        return climbingStyle;
    }

    public void setClimbingStyle(ClimbingStyle climbingStyle) {
        this.climbingStyle = climbingStyle;
    }

    public WallIncline getWallIncline() {
        return wallIncline;
    }

    public void setWallIncline(WallIncline wallIncline) {
        this.wallIncline = wallIncline;
    }

    public String getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(String characteristics) {
        this.characteristics = characteristics;
    }

    public boolean isIndoor() {
        return indoor;
    }

    public void setIndoor(boolean indoor) {
        this.indoor = indoor;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
