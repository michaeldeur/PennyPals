package com.pennypals.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer pennies = 0;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false, unique = true)
    private User user;

    public UserProfile() {}

    public UserProfile(Integer pennies, LocalDateTime updatedAt, User user) {
        this.pennies = pennies;
        this.updatedAt = updatedAt;
        this.user = user;
    }

    @PrePersist
    @PreUpdate
    public void setUpdateTime() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getPennies() { return pennies; }
    public void setPennies(Integer pennies) { this.pennies = pennies; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}