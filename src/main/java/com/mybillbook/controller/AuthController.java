package com.mybillbook.controller;

import com.mybillbook.dto.LoginRequest;
import com.mybillbook.model.User;
import com.mybillbook.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login or register user", description = "Login with mobile number. Creates new user if doesn't exist.")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(
            request.getMobileNumber(),
            request.getName(),
            request.getBusinessName()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("mobileNumber", user.getMobileNumber());
        response.put("name", user.getName());
        response.put("businessName", user.getBusinessName());
        response.put("message", "Login successful");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user details")
    public ResponseEntity<User> getUserDetails(@PathVariable Long userId) {
        User user = authService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
}
