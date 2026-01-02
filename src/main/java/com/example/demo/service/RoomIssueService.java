package com.example.demo.service;

import com.example.demo.model.RoomIssue;
import com.example.demo.repository.RoomIssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.model.User;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoomIssueService {

    @Autowired
    private RoomIssueRepository roomIssueRepository;

    // Save a new report
    public void reportIssue(RoomIssue issue) {
        issue.setStatus("pending"); // Default status
        roomIssueRepository.save(issue);
    }

    // Get all pending issues (For Admin)
    public List<RoomIssue> getPendingIssues() {
        return roomIssueRepository.findByStatus("pending");
    }

    // 1. Get ALL issues (for the Admin list)
    public List<RoomIssue> getAllIssues() {
        return roomIssueRepository.findAll();
    }

    // 2. Resolve an Issue
    public void resolveIssue(Integer issueId, User admin) {
        if (issueId != null) {
        RoomIssue issue = roomIssueRepository.findById(issueId).orElse(null);
        if (issue != null) {
            issue.setStatus("resolved");
            issue.setResolvedBy(admin);
            issue.setResolvedDate(LocalDateTime.now());
            roomIssueRepository.save(issue);
        }
    }
    }
}