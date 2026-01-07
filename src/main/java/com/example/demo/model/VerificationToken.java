package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class VerificationToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String token;
    
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
    
    private LocalDateTime expiryDate;

    public VerificationToken() {}

    public VerificationToken(User user) {
        this.user = user;
        this.token = UUID.randomUUID().toString(); // Generate random code
        this.expiryDate = LocalDateTime.now().plusHours(24); // Valid for 24 hours
    }

    // Getters and Setters
    public String getToken() { return token; }
    public User getUser() { return user; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
}