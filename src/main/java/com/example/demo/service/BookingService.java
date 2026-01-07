package com.example.demo.service;

import com.example.demo.model.Booking;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.example.demo.dto.ReportData;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private JavaMailSender mailSender;
    
    
    public List<Booking> getAllBookings(){
        return bookingRepository.findAll();
    }

    public List<Booking> getPendingBookings(){
        return bookingRepository.findByStatus("pending");
    }

    public long countPendingBookings(){
        return bookingRepository.countByStatus("pending");
    }

    public long countActiveBookings(){
        return bookingRepository.countByStatus("booked");
    }

    public void createBooking(Booking booking, User student) {
        booking.setUser(student);      // Link it to the student
        booking.setStatus("pending");  // Default status
        bookingRepository.save(booking);
    }
    // 1. Method to SAVE the booking to the database
    public void saveBooking(Booking booking) {
        bookingRepository.save(booking);
    }
    public List<Booking> findBookingsByRoomAndDate(String roomId, LocalDate date) {
        // OLD: return bookingRepository.findByRoom_RoomIdAndDate(roomId, date);
        // NEW: Update to match the new repository method name
        return bookingRepository.findByRoom_RoomIdAndSlotDate(roomId, date);
    }
    public List<Booking> findBookingsByWeek(String roomId, LocalDate startOfWeek, LocalDate endOfWeek) {
        // Was: findByRoom_RoomIdAndDateBetween
        return bookingRepository.findByRoom_RoomIdAndSlotDateBetween(roomId, startOfWeek, endOfWeek);
    }

    // Check if a slot is occupied by ANY booking that is NOT cancelled
    public boolean isRoomAvailable(String roomId, LocalDate date, LocalTime start, LocalTime end) {
        // This calls the method we just fixed above, so it should work now.
        List<Booking> conflictBookings = findBookingsByRoomAndDate(roomId, date);
        
        for (Booking b : conflictBookings) {
            // Ignore cancelled bookings
            if (b.getStatus().equalsIgnoreCase("cancelled")) continue;

            // Check for Time Overlap: (StartA < EndB) and (EndA > StartB)
            if (start.isBefore(b.getTimeEnd()) && end.isAfter(b.getTimeStart())) {
                return false; // Conflict found!
            }
        }
        return true; // No conflict
    }
    /*/Update Booking Status (Safe Version)
    public void updateBookingStatus(Integer bookingId, String newStatus, User admin) {
        if (bookingId != null) {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            
            if (booking != null) {
                booking.setStatus(newStatus);
                
                // If approving, track WHO approved it
                if ("booked".equalsIgnoreCase(newStatus)) {
                    booking.setApprovedBy(admin);
                }
                
                bookingRepository.save(booking);
                //trigger emailnotification
                sendNotificationEmail(booking, newStatus);
            }
        }
    }*/

        // NEW METHOD: Handles cancellation with a reason
    public void cancelBooking(Integer bookingId, String reason, User admin) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null) {
            // 1. Update DB Status
            booking.setStatus("cancelled");
            booking.setCancelledBy(admin);
            booking.setCancelReason(reason); // Save reason to DB
            booking.setCancelledAt(java.time.LocalDateTime.now());
            
            bookingRepository.save(booking);

            // 2. Send Email with Reason
            sendNotificationEmail(booking, "cancelled", reason);
        }
    }

    // UPDATE EXISTING METHOD (To keep other parts of your app working)
    public void updateBookingStatus(Integer bookingId, String status, User admin) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null) {
            booking.setStatus(status);
            if ("booked".equals(status)) {
                booking.setApprovedBy(admin);
            }
            bookingRepository.save(booking);
            
            // Pass null for reason for standard updates
            sendNotificationEmail(booking, status, null); 
        }
    }
    private void sendNotificationEmail(Booking booking, String status, String reason) {
        try {
            String userEmail = booking.getUser().getEmail();
            String subject = "Booking Update: " + status.toUpperCase();
            String messageText = "";

            if ("booked".equalsIgnoreCase(status)) {
                messageText = "Dear " + booking.getUser().getFullName() + ",\n\n" +
                        "Good news! Your booking request has been APPROVED.\n\n" +
                        "Details:\n" +
                        "Room: " + booking.getRoom().getName() + "\n" +
                        "Date: " + booking.getSlotDate() + "\n" +
                        "Time: " + booking.getTimeStart() + " - " + booking.getTimeEnd() + "\n\n" +
                        "Please keep this email for your reference.";
            
            } else if ("cancelled".equalsIgnoreCase(status)) {
                // Use the provided reason, or a default one
                String reasonText = (reason != null && !reason.isEmpty()) ? reason : "Administrative Decision";
                
                messageText = "Dear " + booking.getUser().getFullName() + ",\n\n" +
                        "Your booking request has been REJECTED or CANCELLED.\n\n" +
                        "Reason: " + reasonText + "\n\n" +  // <--- NEW: Display Reason
                        "Details:\n" +
                        "Room: " + booking.getRoom().getName() + "\n" +
                        "Date: " + booking.getSlotDate() + "\n\n" +
                        "Please contact the admin if you have questions.";
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmail);
            message.setSubject(subject);
            message.setText(messageText);
            
            mailSender.send(message);
            System.out.println("✅ Email sent to " + userEmail);

        } catch (Exception e) {
            System.err.println("⚠️ Failed to send email: " + e.getMessage());
        }
    }

    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(status);
    }

    public List<Booking> getStudentBookings(User student) {
        return bookingRepository.findByUser(student);
    }

    // Generate Report Data
    public List<ReportData> generateReport(String type, String content, LocalDate selectedDate) {
        List<Booking> allBookings = bookingRepository.findAll();
        List<ReportData> reportResults = new ArrayList<>();

        // 1. Filter by Date Range (Simple logic for now: Filter by Month if 'Monthly', etc.)
        // For simplicity, let's just grab ALL data first, then group it.
        
        if ("MOST_BOOKED".equals(content)) {
            // Group by Room Name and Count
            Map<String, Long> counts = allBookings.stream()
                .filter(b -> !"cancelled".equals(b.getStatus())) // Ignore cancelled
                .collect(Collectors.groupingBy(
                    b -> b.getRoom().getName(), 
                    Collectors.counting()
                ));
            
            counts.forEach((k, v) -> reportResults.add(new ReportData(k, v)));
            
        } else if ("CANCELLED_REJECTED".equals(content)) {
            // Count cancelled bookings per Room
            Map<String, Long> counts = allBookings.stream()
                .filter(b -> "cancelled".equals(b.getStatus()))
                .collect(Collectors.groupingBy(
                    b -> b.getRoom().getName(), 
                    Collectors.counting()
                ));
                
            counts.forEach((k, v) -> reportResults.add(new ReportData(k, v)));
            
        } else {
            // Default: Room Usage (Total Active Bookings)
            Map<String, Long> counts = allBookings.stream()
                .filter(b -> "booked".equals(b.getStatus()))
                .collect(Collectors.groupingBy(
                    b -> b.getRoom().getName(), 
                    Collectors.counting()
                ));
                
            counts.forEach((k, v) -> reportResults.add(new ReportData(k, v)));
        }
        
        // Sort by highest count
        reportResults.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        return reportResults;
    }

    
    public Booking getBookingById(Integer id) {
        return bookingRepository.findById(id).orElse(null);
    }
}
