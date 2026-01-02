package com.example.demo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "bookings")
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private String purpose;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    private String tel;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "time_start", nullable = false)
    private LocalTime timeStart;
    
    @Column(name = "time_end", nullable = false)
    private LocalTime timeEnd;

    //Status pending booked cancelled
    //using strings for now
    @Column(nullable = false)
    private String status = "pending";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /*private LocalDate date;       // e.g., 2025-01-01
    private LocalTime startTime;  // e.g., 08:00
    private LocalTime endTime;*/

    //Cancellation field
    @ManyToOne
    @JoinColumn(name = "cancelled_by")
    private User cancelledBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_reason")
    private String cancelReason;

    //Approve field
    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    //SystemField
    @Column(name = "active_key")
    private String activeKey;

    @Column(name = "session_id")
    private String sessionId;

    
    private boolean recurring = false;
    //Constructor
    public Booking(){}
    
    //getter setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }

   

    public LocalDate getSlotDate() { return slotDate; }
    public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }

    public LocalTime getTimeStart() { return timeStart; }
    public void setTimeStart(LocalTime timeStart) { this.timeStart = timeStart; }

    public LocalTime getTimeEnd() { return timeEnd; }
    public void setTimeEnd(LocalTime timeEnd) { this.timeEnd = timeEnd; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(User cancelledBy) { this.cancelledBy = cancelledBy; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public String getActiveKey() { return activeKey; }
    public void setActiveKey(String activeKey) { this.activeKey = activeKey; }
    
    // Timestamps getters usually don't need setters
    public LocalDateTime getCreatedAt() { return createdAt; }

    public User getApprovedBy() { 
        return approvedBy; 
    }

    public void setApprovedBy(User approvedBy) { 
        this.approvedBy = approvedBy; 
    }

    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }
    /*public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }*/
}
