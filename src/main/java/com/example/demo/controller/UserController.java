package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.Booking;
import com.example.demo.model.Room;
import com.example.demo.service.UserService;
import com.example.demo.service.BookingService;
import com.example.demo.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
public class UserController {
    @Autowired private BookingService bookingService;
    @Autowired private RoomService roomService;
    @Autowired private UserService userService;

    //@Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginPage(){
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user")User user, RedirectAttributes redirectAttributes){
        boolean registrationSuccess = userService.registerNewUser(user);

        if (registrationSuccess){
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! You can now log in.");
            return "redirect:/login";
        }
        else{
            redirectAttributes.addFlashAttribute("errorMessge", "Registration failed! Username or email already exist.");
            return "redirect:/register";
        }
    }

    /*@GetMapping("/dashboard")
    public String showDashboard(){
        return "user/dashboard";
    }*/

    //Forgot Password Here
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(){
        return "auth/forgot-password";
    }

    //send email
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes attributes){
        boolean sent = userService.sendResetLink(email);
        
        if (sent){
            attributes.addFlashAttribute("message", "We have sent a reset link to your email.");
        }else{
            attributes.addFlashAttribute("error", "Email not found.");
        }
        return "redirect:/forgot-password";
    }

    //reset password form
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model){
        User user = userService.getValidUserByToken(token);

        if (user==null){
            model.addAttribute("error", "Invalid or expired token.");
            return "auth/forgot-password";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    //process new password
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token, @RequestParam("password") String password, RedirectAttributes attributes){
        User user = userService.getValidUserByToken(token);

        if(user==null){
            attributes.addFlashAttribute("error", "Invalid Token.");
            return "redirect:/forgot-password";
        }
        userService.updatePassword(user, password);
        attributes.addFlashAttribute("successMessage", "Password successfully reset password. Please log in.");
        return "redirect:/login";
    }

    
    @GetMapping("/student/dashboard")
    public String showStudentDashboard(){
        return "student/dashboard";
    }
    @GetMapping("/lecturer/dashboard")
    public String showLecturerDashboard(){
        return "lecturer/dashboard";
    }

    //Show Profile.html
    @GetMapping("/profile")
    public String showProfilePage(Model model, Principal principal){
        String email = principal.getName();
        User user = userService.findUserByEmail(email);
        model.addAttribute("user", user);
        return "user/profile";
    }
    
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") User formData, 
                                Principal principal, 
                                RedirectAttributes redirectAttributes) {
        String email = principal.getName();
        
        // Update the user
        userService.updateUserProfile(email, formData.getFullName(), formData.getPhoneNumber());
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/profile";
    }

    @PostMapping("/book")
    public String submitBooking(@RequestParam("roomId") String roomId,
                                @RequestParam("date") String dateStr,
                                @RequestParam("startTime") String startTimeStr,
                                @RequestParam("endTime") String endTimeStr,
                                @RequestParam("subject") String subject, // This maps to 'purpose' in your form if names differ
                                @RequestParam("purpose") String purpose,
                                @RequestParam("phone") String phone,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            // 1. Authenticate User
            User currentUser = userService.findUserByEmail(principal.getName());
            Room room = roomService.getRoomById(roomId);

            // 2. Parse Dates and Times
            LocalDate date = LocalDate.parse(dateStr);
            LocalTime start = LocalTime.parse(startTimeStr);
            LocalTime end = LocalTime.parse(endTimeStr);

            // 3. CONFLICT CHECK (The most important part!)
            // We ask the service: "Is this room free at this time?"
            boolean isAvailable = bookingService.isRoomAvailable(roomId, date, start, end);
            
            if (!isAvailable) {
                redirectAttributes.addFlashAttribute("error", "Slot already booked! Please choose another time.");
                return "redirect:/user/view?roomId=" + roomId;
            }

            // 4. Save the Booking
            Booking newBooking = new Booking();
            newBooking.setUser(currentUser);
            newBooking.setRoom(room);
            newBooking.setSlotDate(date);
            newBooking.setTimeStart(start);
            newBooking.setTimeEnd(end);
            newBooking.setPurpose(subject + " - " + purpose); // Combine subject/purpose
            newBooking.setTel(phone);
            newBooking.setStatus("pending"); // Default status is always PENDING
            
            bookingService.saveBooking(newBooking);

            // 5. Send Email Notification (Optional - call your email service here)
            // emailService.sendBookingConfirmation(currentUser, newBooking);

            redirectAttributes.addFlashAttribute("success", "Booking submitted successfully! Status: Pending Approval.");
            return "redirect:/user/dashboard";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred while booking.");
            return "redirect:/user/dashboard";
        }
    }
}
