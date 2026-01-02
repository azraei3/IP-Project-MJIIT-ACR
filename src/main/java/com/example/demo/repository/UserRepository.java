package com.example.demo.repository;

import com.example.demo.model.User; //connect to User model
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    //to check existing email or username during registration or login
    User findByEmail(String email);
    User findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.email = ?1 OR u.username = ?1")
    User findByEmailOrUsername(String loginInput);

    // For forgot password function
    User findByResetToken(String resetToken);
}
