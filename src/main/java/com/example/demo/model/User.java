package com.example.demo.model;

import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "id")
    private Integer userId;

    //@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name= "username", length = 50, unique = true, nullable = false)
    private String username;

    //@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name= "Email", length = 150, unique = true, nullable = false)
    private String email;

    @Column(name="Fullname", length = 100, nullable = false)
    private String fullname;

    @Column(name="password_hash", length = 255, nullable = false)
    private String password;

    @Column(name="User_Type", length = 255, nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        Admin, Lecturer, Student, Staff
    }

    @Column(name="Phone_Number", length = 15, nullable = false)
    private String phoneNumber;

    @CreationTimestamp
    @Column(name="created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;
    
    @Column(name = "enabled")
    private Boolean enabled = false; // Default to false for new registrations

    //Default constructor
    public User(){}

    //getter
    //setter
    public Integer getUserId(){return userId;}
    public void setUserId(Integer userId){this.userId = userId;}

    public String getUsername(){return username;}
    public void setUsername(String username){this.username = username;}

    public String getEmail(){return email;}
    public void setEmail(String email){this.email = email;}

    public String getFullName(){return fullname;}
    public void setFullName(String fullname){this.fullname = fullname;}

    public String getPassword(){return password;}
    public void setPassword(String password){this.password = password;}

    public String getPhoneNumber(){return phoneNumber;}
    public void setPhoneNumber(String phoneNumber){this.phoneNumber = phoneNumber;}
    
    public Role getRole(){return role;}
    public void setRole(Role userType){this.role = userType;}

    public LocalDateTime getCreatedAt(){return createdAt;}
    public LocalDateTime getUpdatedAt(){return updatedAt;}

    public void setCreatedAt(LocalDateTime createdAt){this.createdAt = createdAt;}
    public void setUpdatedAt(LocalDateTime updatedAt){this.updatedAt = updatedAt;}

    public String getResetToken() {return resetToken;}
    public void setResetToken(String resetToken){this.resetToken = resetToken;}

    public LocalDateTime getResetTokenExpiry(){return resetTokenExpiry;}
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry){this.resetTokenExpiry = resetTokenExpiry;}

    // Getter & Setter
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
