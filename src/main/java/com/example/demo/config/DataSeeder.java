package com.example.demo.config;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner demo(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return (args) -> {
            // 1. Check if the Super Admin already exists
            User admin = userRepository.findByEmail("admin@utm.my");

            // 2. If NOT exists, create it
            if (admin == null) {
                User newAdmin = new User();
                newAdmin.setEmail("admin@utm.my");
                newAdmin.setUsername("superadmin");
                newAdmin.setFullName("Super Administrator");
                newAdmin.setPhoneNumber("0123456789");
                newAdmin.setEnabled(true);
                
                // IMPORTANT: Set the password (hashed) and Role
                newAdmin.setPassword(passwordEncoder.encode("admin123")); 
                newAdmin.setRole(User.Role.Admin);

                userRepository.save(newAdmin);
                System.out.println("✅ SUPER ADMIN CREATED: admin@utm.my / admin123");
            } else {
                System.out.println("ℹ️ Super Admin already exists.");
            }
        };
    }
}