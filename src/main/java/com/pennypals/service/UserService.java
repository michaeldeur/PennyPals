package com.pennypals.service;

import com.pennypals.dto.UserRegistrationDto;
import com.pennypals.model.User;
import com.pennypals.model.UserProfile;
import com.pennypals.repository.UserProfileRepository;
import com.pennypals.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserProfileRepository userProfileRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        System.out.println("=== SECURITY LOGIN ATTEMPT FOR: " + usernameOrEmail + " ===");

        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> {
                    System.out.println("=== USER NOT FOUND IN DB ===");
                    return new UsernameNotFoundException("User not found: " + usernameOrEmail);
                });

        System.out.println("=== FOUND USER: " + user.getUsername() + " | HASH: " + user.getPassword() + " ===");

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setSecurityQuestion(dto.getSecurityQuestion());

        // Normalize (trim + lowercase) and hash the security answer securely
        if (dto.getSecurityAnswer() != null && !dto.getSecurityAnswer().isBlank()) {
            String normalizedAnswer = dto.getSecurityAnswer().trim().toLowerCase();
            user.setSecurityAnswer(passwordEncoder.encode(normalizedAnswer));
        }

        User savedUser = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setPennies(100);
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(profile);

        return savedUser;
    }

    public boolean verifySecurityAnswer(String username, String rawInputAnswer) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getSecurityAnswer() == null) {
            return false;
        }
        // Normalize input user attempt before matching against stored hash
        String normalizedInput = rawInputAnswer.trim().toLowerCase();
        return passwordEncoder.matches(normalizedInput, user.getSecurityAnswer());
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public String getSecurityQuestionText(Integer questionId) {
        if (questionId == null) return "Security question not set.";
        switch (questionId) {
            case 1: return "What was the name of your first pet?";
            case 2: return "What elementary school did you attend?";
            case 3: return "What is your mother's maiden name?";
            case 4: return "In what city were you born?";
            default: return "What is your security question?";
        }
    }

    @Transactional
    public void resetPassword(String email, String rawAnswer, String newPassword) {
        User user = findByEmail(email);

        if (user.getSecurityAnswer() == null) {
            throw new IllegalArgumentException("No security answer configured for this account.");
        }

        String normalizedInput = rawAnswer.trim().toLowerCase();
        if (!passwordEncoder.matches(normalizedInput, user.getSecurityAnswer())) {
            throw new IllegalArgumentException("Incorrect security answer.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
