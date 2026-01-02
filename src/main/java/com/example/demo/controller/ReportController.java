package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.model.RoomIssue;
import com.example.demo.model.User;
import com.example.demo.service.RoomIssueService;
import com.example.demo.service.RoomService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/report")
public class ReportController {

    @Autowired private RoomIssueService roomIssueService;
    @Autowired private RoomService roomService;
    @Autowired private UserService userService;

    // 1. Show Report Form
    @GetMapping("/create")
    public String showReportForm(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms()); // Populate dropdown
        return "user/report-issue"; // -> templates/user/report-issue.html
    }

    // 2. Process Submission
    @PostMapping("/create")
    public String submitReport(@RequestParam("roomId") String roomId,
                               @RequestParam("issueType") String issueType,
                               @RequestParam("description") String description,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        
        User reporter = userService.findUserByEmail(principal.getName());
        
        // Find room by ID (Remember ID is String!)
        Room room = roomService.getAllRooms().stream()
                .filter(r -> r.getRoomId().equals(roomId))
                .findFirst()
                .orElse(null);

        if (room != null) {
            RoomIssue issue = new RoomIssue();
            issue.setRoom(room);
            issue.setReportedBy(reporter);
            issue.setIssueType(issueType);
            issue.setDescription(description);
            
            roomIssueService.reportIssue(issue);
            
            redirectAttributes.addFlashAttribute("successMessage", "Report submitted successfully. Thank you!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Room not found.");
        }

        return "redirect:/report/create";
    }
}