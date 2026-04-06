package com.dev.LMS;

import com.dev.LMS.controller.AuthController;
import com.dev.LMS.dto.LoginResponseDto;
import com.dev.LMS.dto.RegisterDto;
import com.dev.LMS.dto.UserLoginDto;
import com.dev.LMS.model.Role;
import com.dev.LMS.model.Student;
import com.dev.LMS.model.User;
import com.dev.LMS.service.UserService;
import com.dev.LMS.util.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UserService userService;

    @Mock
    private UserFactory userFactory;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterSuccess() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setName("John Doe");
        registerDto.setEmail("john.doe@example.com");
        registerDto.setPassword("password123");
        registerDto.setRole("STUDENT");

        User user = new Student("John Doe", "john.doe@example.com");
        user.setPassword("password123");

        when(userFactory.createUser(anyString(), anyString(), anyString())).thenReturn(user);
        doNothing().when(userService).register(any(User.class));

        ResponseEntity<Map<String, String>> response = authController.register(registerDto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("User registered successfully", response.getBody().get("message"));
    }

    @Test
    public void testLoginSuccess() {
        UserLoginDto userLoginDto = new UserLoginDto();
        userLoginDto.setEmail("john.doe@example.com");
        userLoginDto.setPassword("password123");

        Student user = new Student("John Doe", "john.doe@example.com");
        user.setPublicId(UUID.randomUUID());
        user.setRole(Role.STUDENT);

        when(userService.login("john.doe@example.com", "password123")).thenReturn("token");
        when(userService.getUserByEmail("john.doe@example.com")).thenReturn(user);

        ResponseEntity<?> response = authController.login(userLoginDto);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(LoginResponseDto.class, response.getBody());
        LoginResponseDto loginResponse = (LoginResponseDto) response.getBody();
        assertEquals("Login successful", loginResponse.getMessage());
        assertEquals("token", loginResponse.getToken());
        assertEquals("STUDENT", loginResponse.getUser().getLegacyRole());
    }

    @Test
    public void testLoginMissingEmail() {
        UserLoginDto userLoginDto = new UserLoginDto();
        userLoginDto.setEmail(null);
        userLoginDto.setPassword("password123");

        ResponseEntity<?> response = authController.login(userLoginDto);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Email and password are required.", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    public void testLoginMissingPassword() {
        UserLoginDto userLoginDto = new UserLoginDto();
        userLoginDto.setEmail("john.doe@example.com");
        userLoginDto.setPassword(null);

        ResponseEntity<?> response = authController.login(userLoginDto);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Email and password are required.", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    public void testLoginWrongPassword() {
        UserLoginDto userLoginDto = new UserLoginDto();
        userLoginDto.setEmail("john.doe@example.com");
        userLoginDto.setPassword("wrong123");

        when(userService.login("john.doe@example.com", "wrong123"))
                .thenThrow(new RuntimeException("Invalid email or password."));

        ResponseEntity<?> response = authController.login(userLoginDto);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Error: Invalid email or password.", ((Map<?, ?>) response.getBody()).get("message"));
    }
}
