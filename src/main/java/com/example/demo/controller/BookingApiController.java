package com.example.demo.controller;

import com.example.demo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingApiController {

    @Autowired
    private BookingService bookingService;

    // UPDATED: Accept start and end date
    @GetMapping
    public List<BookingDto> getBookings(@RequestParam String roomId, 
                                        @RequestParam String start, 
                                        @RequestParam String end) {
        
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        
        return bookingService.getAllBookings().stream()
                // 1. Filter by Room
                .filter(b -> b.getRoom().getRoomId().equals(roomId))
                
                // 2. Filter by Date Range
                .filter(b -> !b.getSlotDate().isBefore(startDate) && !b.getSlotDate().isAfter(endDate))
                
                // 3. Filter out Cancelled (we don't show these)
                .filter(b -> !b.getStatus().equalsIgnoreCase("cancelled"))
                .map(b -> new BookingDto(
                    b.getSlotDate().toString(), // Send the date back!
                    b.getTimeStart().toString(), 
                    b.getTimeEnd().toString(),
                    b.getStatus(),
                    b.getPurpose()
                ))
                .collect(Collectors.toList());
    }

    // Updated DTO to include Date
    static class BookingDto {
        public String date; // New field
        public String start;
        public String end;
        public String status;
        public String title;

        
        public BookingDto(String date, String start, String end, String status, String title) {
            this.date = date; 
            this.start = start; 
            this.end = end; 
            this.status = status;
            this.title = title;
        }
    }
}