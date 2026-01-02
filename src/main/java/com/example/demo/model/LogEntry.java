package com.example.demo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_logs")
public class LogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id") // The admin who performed the action
    private User user;

    private String action;  // e.g., "APPROVE_BOOKING", "DELETE_ROOM"
    private String details; // e.g., "Approved booking #5 for Room A"

    @CreationTimestamp
    private LocalDateTime timestamp;

    public LogEntry() {}

    public LogEntry(User user, String action, String details) {
        this.user = user;
        this.action = action;
        this.details = details;
    }

    // Getters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public LocalDateTime getTimestamp() { return timestamp; }
}