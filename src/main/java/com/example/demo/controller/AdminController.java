package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.LoginRequest;
import com.example.demo.security.JwtUtil;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/admin")
public class AdminController {

//    private String ADMIN_USERNAME = "admin";
//
//    // example password = xerox123    $2a$10$JnyXgENUmJxnthhg170bwe/6/i.xySdJy1CpieJjea9/MavSXEKly
//
//    private String ADMIN_PASSWORD = "$2a$10$JnyXgENUmJxnthhg170bwe/6/i.xySdJy1CpieJjea9/MavSXEKly";
	@Value("${admin.username}")
	private String ADMIN_USERNAME;

	@Value("${admin.password}")
	private String ADMIN_PASSWORD;

    private int failedAttempts = 0;
    private long lockTime = 0;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Map<String, Object> response = new HashMap<>();

        // check if account locked
        if (failedAttempts >= 5 && System.currentTimeMillis() < lockTime) {
            response.put("success", false);
            response.put("message", "Too many attempts. Try again in 5min.");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
        }

        if (!request.getUsername().equals(ADMIN_USERNAME)) {
            response.put("success", false);
            response.put("message", "Invalid username");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if (!encoder.matches(request.getPassword(), ADMIN_PASSWORD)) {

            failedAttempts++;

            if (failedAttempts >= 5) {
                lockTime = System.currentTimeMillis() + (5 * 60 * 1000); // 10 minutes
            }

            response.put("success", false);
            response.put("message", "Invalid password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // reset attempts on success
        failedAttempts = 0;

//        response.put("success", true);
////        response.put("token", "admin-session");
//        String token = UUID.randomUUID().toString();
//        response.put("token", token);
//        response.put("message", "Login successful");
        String token = JwtUtil.generateToken(request.getUsername());

        response.put("success", true);
        response.put("token", token);
        response.put("message", "Login successful");

        return ResponseEntity.ok(response);
//        String token = UUID.randomUUID().toString();
//        response.put("token", token);
    }
    
}