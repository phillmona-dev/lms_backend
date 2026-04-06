package com.dev.LMS.controller;

import com.dev.LMS.dto.NotificationDto;
import com.dev.LMS.dto.RegisterDto;
import com.dev.LMS.dto.UpdateProfileDto;
import com.dev.LMS.dto.UserLoginDto;
import com.dev.LMS.model.Instructor;
import com.dev.LMS.model.Student;
import com.dev.LMS.model.User;
import com.dev.LMS.service.NotificationService;
import com.dev.LMS.service.UserService;
import com.dev.LMS.util.UserFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Endpoints for profile management, user administration, and user notifications.")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserFactory userFactory;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Returns the authenticated user's profile information.")
    public ResponseEntity<User> getUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user profile", description = "Updates the authenticated user's profile information including name, email, and password.")
    public ResponseEntity<User> updateUserProfile(@RequestBody UpdateProfileDto updateProfileDto) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User updatedUser = userService.updateUser(email, updateProfileDto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/users")
    @Operation(summary = "List all users", description = "Returns all users in the system. This endpoint is intended for administrators.")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get a user by numeric id", description = "Returns a single user record using the legacy numeric user id.")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users/create")
    @Operation(summary = "Create a user as admin", description = "Creates a new user account from the user management area using the supplied role and credentials.")
    public ResponseEntity<Map<String, String>> createUser(@Valid @RequestBody RegisterDto registerDto) {
        Map<String, String> response = new HashMap<>();
        try {
            User user = userFactory.createUser(registerDto.getRole(), registerDto.getName(), registerDto.getEmail());
            user.setPassword(registerDto.getPassword());

            userService.register(user);
            response.put("message", "User registered successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/users/delete/{id}")
    @Operation(summary = "Delete a user", description = "Deletes a user by legacy numeric user id.")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/notifications")
    @Operation(summary = "Get current user notifications", description = "Returns notifications for the authenticated student or instructor.")
    public ResponseEntity<?> getAllNotifications() {
        try{
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            if (user instanceof Instructor){
                Instructor instructor = (Instructor) user;
                List<NotificationDto> notifications = notificationService.getInstructorNotification(instructor);
                return ResponseEntity.ok(notifications);
            }
            if (user instanceof Student){
                Student student = (Student) user;
                List<NotificationDto> notifications = notificationService.getStudentNotification(student);
                return ResponseEntity.ok(notifications);
            }
            else
                return ResponseEntity.badRequest().body("User not found");
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
