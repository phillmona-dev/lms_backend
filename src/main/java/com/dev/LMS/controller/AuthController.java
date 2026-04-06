package com.dev.LMS.controller;

import com.dev.LMS.dto.UserLoginDto;
import com.dev.LMS.dto.LoginResponseDto;
import com.dev.LMS.dto.RbacUserDto;
import com.dev.LMS.model.User;
import com.dev.LMS.service.UserService;
import com.dev.LMS.util.UserFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dev.LMS.dto.RegisterDto;

import java.util.HashMap;
import java.util.Map;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for registering users and signing in with JWT tokens.")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserFactory userFactory;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new LMS user account using one of the supported actor roles such as STUDENT, TEACHER, PARENT, SCHOOL_ADMINISTRATOR, or SYSTEM_ADMINISTRATOR.")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterDto registerDto) {
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

    @PostMapping("/login")
    @Operation(summary = "Authenticate a user", description = "Authenticates the user with email and password, then returns a JWT token and the logged-in user's role and authority details.")
    public ResponseEntity<?> login(@RequestBody UserLoginDto userdto) {
        Map<String, String> response = new HashMap<>();
        try {
            if (userdto.getEmail() == null || userdto.getPassword() == null) {
                response.put("message", "Email and password are required.");
                return ResponseEntity.badRequest().body(response);
            }
            String token = userService.login(userdto.getEmail(), userdto.getPassword());
            User loggedInUser = userService.getUserByEmail(userdto.getEmail());
            LoginResponseDto loginResponse = new LoginResponseDto(
                    "Login successful",
                    token,
                    new RbacUserDto(loggedInUser)
            );
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
