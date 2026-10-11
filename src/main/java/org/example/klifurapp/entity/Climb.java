package org.example.klifurapp.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "climbs")
public class Climb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyGrade difficultyGrade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClimbingStyle climbingStyle;

    @Column(nullable = false)
    private LocalDate date;

    private String notes;

    @Enumerated(EnumType.STRING)
    private WallIncline wallIncline;

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