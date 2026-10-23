package com.pennypals.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal currentProgress = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean isCompleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    public Budget() {}

    public Budget(String type, String category, BigDecimal monthlyLimit, BigDecimal currentProgress, Boolean isCompleted, User user) {
        this.type = type;
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        if (currentProgress != null) this.currentProgress = currentProgress;
        if (isCompleted != null) this.isCompleted = isCompleted;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }
    public BigDecimal getCurrentProgress() { return currentProgress; }
    public void setCurrentProgress(BigDecimal currentProgress) { this.currentProgress = currentProgress; }
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}