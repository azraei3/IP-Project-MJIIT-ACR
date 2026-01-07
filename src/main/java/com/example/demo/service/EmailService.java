package com.example.demo.service;

import com.example.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Inside EmailService.java

    public void sendVerificationEmail(User user, String token) {
        String subject = "Verify your Email";
        String verificationUrl = "http://localhost:8080/verify?token=" + token;
        
        String message = "Dear " + user.getFullName() + ",\n\n" +
                         "Please click the link below to verify your account:\n" +
                         verificationUrl + "\n\n" +
                         "If you did not request this, please ignore this email.";
                         
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(user.getEmail());
        email.setSubject(subject);
        email.setText(message);
        mailSender.send(email);
    }
}