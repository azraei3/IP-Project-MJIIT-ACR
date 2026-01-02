package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    //@Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository =userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean registerNewUser(User user){

        if(userRepository.findByEmail(user.getEmail()) != null){
            System.out.println("Registration failed: Email already exists.");
            return false;
        }

        if(userRepository.findByUsername(user.getUsername()) !=null){
            System.out.println("Registration failed: Username already exists.");
            return false;
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        if (user.getRole() == null){
            user.setRole(User.Role.Student);
        }

        userRepository.save(user);
        return true;
    }

    //updates profile of the currently logged in user
    public void updateUserProfile(String email, String newFullName, String newPhoneNumber){
        User user = userRepository.findByEmail(email);

        if(user!=null){
            user.setFullName(newFullName);
            user.setPhoneNumber(newPhoneNumber);

            userRepository.save(user);
        }
    }
    
    public User findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public boolean sendResetLink(String email){
        User user = userRepository.findByEmail(email);
        if (user==null){
            return false;
        }
        //Generate Random token
        String token = UUID.randomUUID().toString();
        //Set timer 30 Minutes
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);
        //Send Email
        String resetUrl = "http://localhost:8080/reset-password?token=" + token;
        sendEmail(user.getEmail(), resetUrl);

        return true;
    }

    //Email send format 
    private void sendEmail(String to, String resetUrl){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset REquest");
        message.setText("Click the link to reset your password: " + resetUrl + "\n\nLink Expires in 30 minutes.");
        mailSender.send(message);
    }

    //validate token when user clicks the link
    public User getValidUserByToken(String token){
        User user = userRepository.findByResetToken(token);
        if (user==null)return null;

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())){
            return null;
        }
        return user;
    }

    public void updatePassword(User user, String newPassword){
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    // 1. Get All Users (for the list)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 2. Update User Role (Promote/Demote)
    public void updateUserRole(Integer userId, String roleName) {
        if (userId != null && roleName != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setRole(User.Role.valueOf(roleName)); 
                userRepository.save(user);
            }
        }
    }

    // 3. Delete User
    public void deleteUser(Integer userId) {
        if(userId != null){userRepository.deleteById(userId);}
    }

    // 4. Count Total Users
    public long countUsers() {
        return userRepository.count(); // Built-in JPA method
    }
}
