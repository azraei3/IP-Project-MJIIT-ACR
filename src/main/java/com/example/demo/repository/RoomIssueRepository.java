package com.example.demo.repository;

import com.example.demo.model.RoomIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomIssueRepository extends JpaRepository<RoomIssue, Integer> {
    // Find all pending issues (for Admin dashboard)
    List<RoomIssue> findByStatus(String status);
}