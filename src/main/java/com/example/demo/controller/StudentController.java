package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.model.Room;
import com.example.demo.model.User;
import com.example.demo.service.BookingService;
import com.example.demo.service.RoomService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
@RequestMapping("/user") // Base URL for students
public class StudentController {

    @Autowired private BookingService bookingService;
    @Autowired private UserService userService;
    @Autowired private RoomService roomService;

    // ==========================================
    // 1. RESERVATION LIST (My History)
    // ==========================================
    // Inside StudentController.java

    // ==========================================
    // 1. RESERVATION LIST (My History) with Filters
    // ==========================================
    @GetMapping("/reservations")
    public String showReservations(@RequestParam(value = "status", defaultValue = "all") String status,
                                   Model model, 
                                   Principal principal) {
        
        // 1. Get logged-in user
        User student = userService.findUserByEmail(principal.getName());
        
        // 2. Get ALL bookings for this student
        List<Booking> allBookings = bookingService.getStudentBookings(student);
        
        // 3. Filter Logic (Java Stream)
        List<Booking> filteredBookings;
        
        if ("pending".equalsIgnoreCase(status)) {
            filteredBookings = allBookings.stream()
                .filter(b -> "pending".equalsIgnoreCase(b.getStatus()))
                .toList();
                
        } else if ("approved".equalsIgnoreCase(status)) {
            // Database uses "booked" for approved status
            filteredBookings = allBookings.stream()
                .filter(b -> "booked".equalsIgnoreCase(b.getStatus())) 
                .toList();
                
        } else if ("cancelled".equalsIgnoreCase(status)) {
            // Database uses "cancelled" for rejected/cancelled
            filteredBookings = allBookings.stream()
                .filter(b -> "cancelled".equalsIgnoreCase(b.getStatus()))
                .toList();
        } else {
            // Default: Show ALL
            filteredBookings = allBookings;
        }

        // 4. Calculate Counts for the Badges (Optional but looks nice)
        long countPending = allBookings.stream().filter(b -> "pending".equalsIgnoreCase(b.getStatus())).count();
        long countApproved = allBookings.stream().filter(b -> "booked".equalsIgnoreCase(b.getStatus())).count();
        long countCancelled = allBookings.stream().filter(b -> "cancelled".equalsIgnoreCase(b.getStatus())).count();

        model.addAttribute("bookings", filteredBookings);
        model.addAttribute("currentStatus", status); // To highlight the active tab
        
        // Pass counts for the UI badges
        model.addAttribute("countPending", countPending);
        model.addAttribute("countApproved", countApproved);
        model.addAttribute("countCancelled", countCancelled);
        model.addAttribute("countAll", allBookings.size());

        return "student/reservations/list";
    }

    @PostMapping("/reservations/cancel")
    public String cancelBooking(@RequestParam("bookingId") Integer bookingId, Principal principal) {
        User student = userService.findUserByEmail(principal.getName());
        // Reuse the update status method (change status to 'cancelled')
        bookingService.updateBookingStatus(bookingId, "cancelled", student);
        return "redirect:/user/reservations";
    }

    // ==========================================
    // 2. TIMETABLE & BOOKING FORM
    // ==========================================
    /*@GetMapping("/timetable")
    public String showTimetable(Model model) {
        // We need to send the list of Rooms to the dropdown
        model.addAttribute("rooms", roomService.getAllRooms());
        return "student/timetable/view";
    }*/
    @GetMapping("/timetable")
    public String showStudentTimetable(@RequestParam(value = "roomId", required = false) String roomId,
                                       @RequestParam(value = "date", required = false) String dateStr,
                                       Model model,
                                       Principal principal) {
        
        // 1. Load Rooms
        model.addAttribute("rooms", roomService.getAllRooms());

        if (roomId == null || dateStr == null || dateStr.isEmpty()) {
            return "student/timetable/view"; // Name of the user view file
        }

        // 2. Calculate Week Range
        LocalDate selectedDate = LocalDate.parse(dateStr);
        LocalDate startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        // 3. Fetch Bookings
        List<Booking> bookings = bookingService.findBookingsByWeek(roomId, startOfWeek, endOfWeek);

        // 4. Map Generation (Exact match to Admin logic)
        Map<String, Booking> scheduleMap = new HashMap<>();
        
        // Get current logged-in user to check ownership
        User currentUser = userService.findUserByEmail(principal.getName());

        for (Booking b : bookings) {

            // Skip cancelled bookings so students see the slot as available
            if ("cancelled".equalsIgnoreCase(b.getStatus())) {
                continue;
            }

            String dateKey = b.getSlotDate().toString(); 
            int startHour = b.getTimeStart().getHour();
            int endHour = b.getTimeEnd().getHour();

            for (int h = startHour; h < endHour; h++) {
                // Key Format: "2026-01-02_08:00"
                String timeString = String.format("%02d:00", h); 
                String fullKey = dateKey + "_" + timeString;
                scheduleMap.put(fullKey, b);
            }
        }

        // 5. Row Headers (Days)
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
        model.addAttribute("selectedRoomName", roomService.getRoomById(roomId).getName());
        
        // Pass current username to view so we can highlight "My Bookings"
        model.addAttribute("currentUsername", currentUser.getUsername()); 
        // This allows the HTML to compare booking.user.username == currentUsername
    model.addAttribute("currentUsername", principal.getName());
        return "student/timetable/view";
    }

    @PostMapping("/book")
    public String processBooking(@RequestParam("roomId") String roomId,
                                 @RequestParam("date") String dateStr,     // Format: YYYY-MM-DD
                                 @RequestParam("slots") String slotsJson,
                                 @RequestParam("purpose") String purpose,
                                 @RequestParam("description") String description,
                                 @RequestParam("phone") String phone,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            User student = userService.findUserByEmail(principal.getName());
            Room room = roomService.getAllRooms().stream()
                .filter(r -> r.getRoomId().equals(roomId))
                .findFirst().orElse(null);

            if (room != null) {
                // 2. Parse Slots (e.g., ["08:00", "09:00"] -> Start: 08:00, End: 10:00)
                String[] timeSlots = slotsJson.replace("[", "").replace("]", "").replace("\"", "").split(",");
                LocalTime startTime = LocalTime.parse(timeSlots[0].trim());
                LocalTime endTime = LocalTime.parse(timeSlots[timeSlots.length-1].trim()).plusHours(1);

                // 3. Conflict Check
                boolean isAvailable = bookingService.isRoomAvailable(roomId, LocalDate.parse(dateStr), startTime, endTime);
                if (!isAvailable) {
                    redirectAttributes.addFlashAttribute("error", "Slot already booked! Please choose another time.");
                    return "redirect:/user/timetable?roomId=" + roomId + "&date=" + dateStr;
                }

                // 4. Save Booking
                Booking booking = new Booking();
                booking.setRoom(room);
                booking.setSlotDate(LocalDate.parse(dateStr));
                booking.setTimeStart(startTime);
                booking.setTimeEnd(endTime);
                booking.setPurpose(purpose);
                booking.setDescription(description);
                booking.setTel(student.getPhoneNumber()); // Use phone from User profile
                booking.setStatus("pending");

                bookingService.createBooking(booking, student);
                redirectAttributes.addFlashAttribute("successMessage", "Booking requested successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing booking.");
        }

        return "redirect:/user/reservations";
    }
}