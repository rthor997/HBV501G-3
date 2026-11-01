package org.example.klifurapp.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "climbs")
public class Climb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GradingSystem gradingSystem;

    @Column(nullable = false)
    private String difficultyGrade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClimbingStyle climbingStyle;

    @Column(nullable = false)
    private LocalDate date;

    private String notes;

    @Enumerated(EnumType.STRING)
    private WallIncline wallIncline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    public Climb() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public GradingSystem getGradingSystem() {
        return gradingSystem;
    }

    public void setGradingSystem(GradingSystem gradingSystem) {
        this.gradingSystem = gradingSystem;
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

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }
}