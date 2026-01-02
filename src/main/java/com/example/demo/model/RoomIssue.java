package com.example.demo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_issues")
public class RoomIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_id")
    private Integer issueId;

    // Foreign Key: Which room has the problem?
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // Foreign Key: Who reported it?
    @ManyToOne
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    // Enum for Issue Type (e.g., HVAC, Projector, Cleaning)
    @Column(name = "issue_type", nullable = false)
    private String issueType; 

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    // Status: 'pending', 'in_progress', 'resolved'
    @Column(nullable = false)
    private String status = "pending";

    @CreationTimestamp
    @Column(name = "reported_date", updatable = false)
    private LocalDateTime reportedDate;

    // Who resolved it? (Nullable)
    @ManyToOne
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolved_date")
    private LocalDateTime resolvedDate;

    // --- Constructors ---
    public RoomIssue() {}

    // --- Getters and Setters ---
    public Integer getIssueId() { return issueId; }
    public void setIssueId(Integer issueId) { this.issueId = issueId; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public User getReportedBy() { return reportedBy; }
    public void setReportedBy(User reportedBy) { this.reportedBy = reportedBy; }

    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getReportedDate() { return reportedDate; }
    
    public User getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(User resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getResolvedDate() { return resolvedDate; }
    public void setResolvedDate(LocalDateTime resolvedDate) { this.resolvedDate = resolvedDate; }
}