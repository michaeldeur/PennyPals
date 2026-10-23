package com.pennypals.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private Integer securityQuestion;

    @Column(nullable = false)
    private String securityAnswer;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile userProfile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Budget> budgets = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UnlockedItem> unlockedItems = new ArrayList<>();

    public User() {}

    public User(String username, String password, String email, Integer securityQuestion, String securityAnswer) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(Integer securityQuestion) { this.securityQuestion = securityQuestion; }
    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }
    public UserProfile getUserProfile() { return userProfile; }
    public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }
    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    public List<Budget> getBudgets() { return budgets; }
    public void setBudgets(List<Budget> budgets) { this.budgets = budgets; }
    public List<UnlockedItem> getUnlockedItems() { return unlockedItems; }
    public void setUnlockedItems(List<UnlockedItem> unlockedItems) { this.unlockedItems = unlockedItems; }
}