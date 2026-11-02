package com.pennypals;

import com.pennypals.model.User;
import com.pennypals.model.UserProfile;
import com.pennypals.repository.UserProfileRepository;
import com.pennypals.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

@SpringBootApplication
public class PennyPalsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PennyPalsApplication.class, args);
    }

    @Bean
    CommandLineRunner initData(UserRepository userRepository, UserProfileRepository userProfileRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("demo_user").isEmpty()) {
                User user = new User();
                user.setUsername("demo_user");
                user.setEmail("demo@pennypals.com");
                user.setPassword(passwordEncoder.encode("password123"));
                user.setSecurityQuestion(1);
                user.setSecurityAnswer(passwordEncoder.encode("Blue")); // <-- Hashed for security consistency
                User savedUser = userRepository.save(user);

                UserProfile profile = new UserProfile();
                profile.setUser(savedUser);
                profile.setPennies(150);
                profile.setUpdatedAt(LocalDateTime.now());
                userProfileRepository.save(profile);

                System.out.println("=== DEMO USER SEEDED SUCCESSFULLY ===");
            }
        };
    }
}