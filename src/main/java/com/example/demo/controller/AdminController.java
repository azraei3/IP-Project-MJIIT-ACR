package com.example.demo.controller;

import com.example.demo.service.RoomIssueService;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.model.Room;
import com.example.demo.service.RoomService; // Add import
import com.example.demo.model.Booking;
import com.example.demo.service.BookingService;
import com.example.demo.model.LogEntry;
import com.example.demo.service.LogService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.dto.ReportData; // Import the DTO

import java.time.LocalDate;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UserService userService;
    @Autowired private RoomService roomService;
    @Autowired private BookingService bookingService;
    @Autowired private RoomIssueService roomIssueService;
    @Autowired private LogService logService;
    // ========= BATCH 2: DASHBOARD =========
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        long totalUsers = userService.countUsers();
        long totalRooms = roomService.countRooms();
        long activeBookings = bookingService.countActiveBookings(); 
        long pendingApprovals = bookingService.countPendingBookings();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalRooms", totalRooms);
        stats.put("activeBookings", activeBookings);
        stats.put("pendingApprovals", pendingApprovals);
        model.addAttribute("stats", stats);
        return "admin/dashboard"; // -> templates/admin/dashboard.html
    }

    // 1. Show the Report Wizard (usage.html)
    @GetMapping("/reports/usage")
    public String showUsageReportPage(Model model) {
        return "admin/reports/usage"; 
    }

    // 2. Generate and Show Results
    @PostMapping("/reports/generate")
    public String generateReport(@RequestParam(value = "selectedDate", required = false) String dateStr,
                                 @RequestParam("reportType") String type,
                                 @RequestParam("reportContent") String content,
                                 Model model) {
        System.out.println("✅ Report Generation Triggered!");
        LocalDate date = (dateStr != null && !dateStr.isEmpty()) 
                         ? LocalDate.parse(dateStr) 
                         : LocalDate.now();

        List<ReportData> results = bookingService.generateReport(type, content, date);
        
        model.addAttribute("results", results);
        model.addAttribute("reportTitle", content.replace("_", " ") + " (" + type + ")");
        
        System.out.println("✅ Data calculated, sending to view...");
        return "admin/reports/result";
    }

    // --- MANAGE ROOMS ---
    @GetMapping("/rooms")
    public String listRooms(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("newRoom", new Room()); // For the "Add Room" form
        return "admin/rooms"; // -> templates/admin/rooms.html
    }

    /*@PostMapping("/rooms/save")
    public String saveRoom(@ModelAttribute("newRoom") Room room) {
        roomService.saveRoom(room);
        return "redirect:/admin/rooms";
    }*/
    @PostMapping("/rooms/save")
    public String saveRoom(@ModelAttribute("newRoom") Room room, Principal principal) {
        User admin = userService.findUserByEmail(principal.getName());
        roomService.saveRoom(room);
        // --- ADD LOG ---
        logService.log(admin, "CREATE_ROOM", "Created Room " + room);
        return "redirect:/admin/rooms";
    }
    /*@PostMapping("/rooms/delete")
    public String deleteRoom(@RequestParam("roomId") String roomId) {
        roomService.deleteRoom(roomId);
        return "redirect:/admin/rooms";
    }*/
   // Example: Delete Room
    @PostMapping("/rooms/delete")
    public String deleteRoom(@RequestParam("roomId") String roomId, Principal principal) {
        User admin = userService.findUserByEmail(principal.getName());
        roomService.deleteRoom(roomId);
        
        // --- ADD LOG ---
        logService.log(admin, "DELETE_ROOM", "Deleted Room " + roomId);
        
        return "redirect:/admin/rooms";
    }
    

    // ========= UPDATED: TIMETABLE MANAGEMENT =========
    
    @GetMapping("/timetable")
    public String showTimetableManagement(@RequestParam(value = "roomId", required = false) String roomId, // Keep as String
                                          @RequestParam(value = "date", required = false) String dateStr,
                                          Model model) {
        
        model.addAttribute("rooms", roomService.getAllRooms());

        if (roomId == null || dateStr == null || dateStr.isEmpty()) {
            return "admin/timetable/manage";
        }

        // REMOVED: Integer.parseInt(roomId); -> We use the String 'roomId' directly now

        // DATE LOGIC
        LocalDate selectedDate = LocalDate.parse(dateStr);
        LocalDate startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        // FETCH BOOKINGS (Passing String roomId)
        List<Booking> bookings = bookingService.findBookingsByWeek(roomId, startOfWeek, endOfWeek); 

        // MAP GENERATION
        Map<String, Booking> scheduleMap = new HashMap<>();

        // Inside showTimetableManagement method 
        for (Booking b : bookings) {
            // 2. Ensure you use the getters for the fields that have data
            String dateKey = b.getSlotDate().toString(); 
            int startHour = b.getTimeStart().getHour();
            int endHour = b.getTimeEnd().getHour();

            for (int h = startHour; h < endHour; h++) {
                // 3. Format key: "08:00"
                String timeString = String.format("%02d:00", h); 
                String fullKey = dateKey + "_" + timeString;
                scheduleMap.put(fullKey, b);
            }
        }
        // ROW GENERATION
        Map<String, String> weekDates = new LinkedHashMap<>();
        LocalDate current = startOfWeek;
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        
        for (String dayName : days) {
            weekDates.put(dayName, current.toString());
            current = current.plusDays(1);
        }

        model.addAttribute("scheduleMap", scheduleMap);
        model.addAttribute("weekDates", weekDates); 
        model.addAttribute("weekDays", days);       
        
        model.addAttribute("selectedRoomId", roomId);
        model.addAttribute("selectedDate", dateStr);
        
        // Ensure getRoomById accepts String (it should, based on your errors)
        Room room = roomService.getRoomById(roomId); 
        model.addAttribute("selectedRoomName", room.getName());

        return "admin/timetable/manage"; 
    }

    // ========= NEW: HANDLE ADMIN BOOKING CREATION =========
    @PostMapping("/create-class")
    public String createClass(@RequestParam("roomId") String roomId,
                              @RequestParam("date") String dateStr,
                              @RequestParam("slots") String slotsJson,
                              @RequestParam("subject") String subject,
                              @RequestParam("description") String description,
                              // NEW: Accept Recurrence Params
                              @RequestParam(value = "recurrence", defaultValue = "NONE") String recurrence,
                              @RequestParam(value = "endDate", required = false) String endDateStr,
                              Principal principal) {
        User admin = userService.findUserByEmail(principal.getName());
        // 1. Parse Initial Date & Times
        LocalDate startDate = LocalDate.parse(dateStr);
        
        // Calculate Time Range from slots (e.g., ["08:00", "09:00"] -> 08:00 to 10:00)
        String[] timeSlots = slotsJson.replace("[", "").replace("]", "").replace("\"", "").split(",");
        LocalTime startTime = LocalTime.parse(timeSlots[0].trim());
        LocalTime endTime = LocalTime.parse(timeSlots[timeSlots.length-1].trim()).plusHours(1);

        // 2. Determine Recurrence End Date
        LocalDate recurUntil = startDate; // Default to single day
        if (!"NONE".equalsIgnoreCase(recurrence) && endDateStr != null && !endDateStr.isEmpty()) {
            recurUntil = LocalDate.parse(endDateStr);
        }

        // 3. Loop and Create Bookings
        LocalDate currentDate = startDate;
        
        Room room = roomService.getRoomById(roomId);

        while (!currentDate.isAfter(recurUntil)) {
            // Check availability before booking (Optional but recommended)
            if (bookingService.isRoomAvailable(roomId, currentDate, startTime, endTime)) {
                
                Booking newBooking = new Booking();
                newBooking.setRoom(room);
                newBooking.setUser(admin);
                newBooking.setSlotDate(currentDate); // Set the specific date for this iteration
                newBooking.setTimeStart(startTime);
                newBooking.setTimeEnd(endTime);
                newBooking.setPurpose(subject + " - " + description);
                newBooking.setStatus("booked"); // Auto-approve
                newBooking.setTel("ADMIN");     // Marker for admin bookings
                
                bookingService.saveBooking(newBooking);
            }

            // 4. Increment Date
            if ("WEEKLY".equalsIgnoreCase(recurrence)) {
                currentDate = currentDate.plusWeeks(1);
            } else if ("MONTHLY".equalsIgnoreCase(recurrence)) {
                currentDate = currentDate.plusMonths(1);
            } else {
                break; // NONE = Stop after first run
            }
        }
        logService.log(admin, "CREATE_CLASS", 
        "Created class for Room: " + roomId + " on " + dateStr + ". Subject: " + subject);
        return "redirect:/admin/timetable?roomId=" + roomId + "&date=" + dateStr;
    }

    // ========= NEW: USER MANAGEMENT =========

    // 1. Show User List
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users"; // -> templates/admin/users.html
    }

    // 2. Handle Role Update
    @PostMapping("/users/update")
    public String updateUserRole(@RequestParam("userId") Integer userId, 
                                 @RequestParam("role") String role, 
                                Principal principal) {
        User admin = userService.findUserByEmail(principal.getName());
        
        userService.updateUserRole(userId, role);
        logService.log(admin, "User Role Update" , "User ID:" + userId + "to" + role);
        return "redirect:/admin/users";
    }

    // 3. Handle Delete
    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam("userId") Integer userId) {
        userService.deleteUser(userId);
        return "redirect:/admin/users";
    }
    // --- MANAGE BOOKING REQUESTS ---

    // 1. Show List of Pending Requests
    // NEW VERSION (With Status Filter Logic)
    @GetMapping("/bookings")
    public String listBookingRequests(@RequestParam(value = "status", defaultValue = "pending") String status, 
                                      @RequestParam(value = "roomId", required = false) String roomId, // 1. Add roomId param
                                      Model model) {
        
        // 2. Fetch Rooms for the dropdown
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("selectedRoomId", roomId); // Keep selection after reload

        List<Booking> bookings;
        
        // 3. Filter by Status (Existing Logic)
        if ("all".equalsIgnoreCase(status)) {
            bookings = bookingService.getAllBookings();
        } else if ("approved".equalsIgnoreCase(status)) {
            bookings = bookingService.getBookingsByStatus("booked");
        } else if ("rejected".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)) {
            bookings = bookingService.getBookingsByStatus("cancelled");
        } else {
            bookings = bookingService.getPendingBookings();
        }

        // 4. NEW: Filter by Room (if selected)
        if (roomId != null && !roomId.isEmpty()) {
            bookings = bookings.stream()
                .filter(b -> b.getRoom().getRoomId().equals(roomId))
                .toList(); // Requires Java 16+, use .collect(Collectors.toList()) for older Java
        }
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("pendingCount", bookingService.countPendingBookings());
        model.addAttribute("currentStatus", status); 
        
        return "admin/bookings/list"; 
    }

    // 2. Approve Booking
    @PostMapping("/bookings/approve")
    public String approveBooking(@RequestParam("bookingId") Integer bookingId, Principal principal) {
        // Get the currently logged-in Admin
        User admin = userService.findUserByEmail(principal.getName());
        
        // Update status to 'booked' (Active)
        bookingService.updateBookingStatus(bookingId, "booked", admin);

        logService.log(admin, "APPROVE_BOOKING", "Approved booking ID #" + bookingId);

        return "redirect:/admin/bookings";
    }

    // 3. Reject Booking
    @PostMapping("/bookings/reject")
    public String rejectBooking(@RequestParam("bookingId") Integer bookingId, Principal principal) {
        User admin = userService.findUserByEmail(principal.getName());
        // Update status to 'cancelled' (Rejected)
        bookingService.updateBookingStatus(bookingId, "cancelled", admin);
        
        logService.log(admin, "REJECT_BOOKING", "REJECT booking ID #" + bookingId);

        return "redirect:/admin/bookings";
    }

    // --- MANAGE ISSUES ---

    // 1. Show Issue List
    @GetMapping("/issues")
    public String listIssues(Model model) {
        model.addAttribute("issues", roomIssueService.getAllIssues());
        return "admin/issues"; // -> templates/admin/issues.html
    }

    // 2. Mark as Resolved
    @PostMapping("/issues/resolve")
    public String resolveIssue(@RequestParam("issueId") Integer issueId, Principal principal) {
        User admin = userService.findUserByEmail(principal.getName());
        roomIssueService.resolveIssue(issueId, admin);
        return "redirect:/admin/issues";
    }

    //LOG BUUK BUUK
    @GetMapping("/logbook")
    public String showLogbook(Model model) {
        model.addAttribute("logs", logService.getAllLogs());
        return "admin/audit/logbook";
    }

    
}
