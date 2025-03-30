package com.unishop.unishop_backend.controller;

import com.unishop.unishop_backend.entity.User;
import com.unishop.unishop_backend.model.UserRole;
import com.unishop.unishop_backend.repository.UserRepository;
import com.unishop.unishop_backend.utils.JwtUtil;
import exception.user.UserErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.get("username"), request.get("password")
                    )
            );
        }
        catch (AuthenticationException ex) {
            UserErrorResponse errorResponse = new UserErrorResponse();
            errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            errorResponse.setMessage("Invalid username or password");
            errorResponse.setTimeStamp(System.currentTimeMillis());

            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
        String token = jwtUtil.generateToken(request.get("username"));
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {

            UserErrorResponse errorResponse = new UserErrorResponse();
            errorResponse.setStatus(HttpStatus.CONFLICT.value());
            errorResponse.setMessage("Username is already in use");
            errorResponse.setTimeStamp(System.currentTimeMillis());

            return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() != null) {
            user.setRole(user.getRole());
        }
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

//    @DeleteMapping("/delete")
//    public ResponseEntity<?> delete(@RequestBody User user) {
//        Optional<User> userOptional = userRepository.findByUsername(user.getUsername());
//        if (userOptional.isPresent()) {
//            User existingUser = userOptional.get();
//            if (passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
//                userRepository.delete(existingUser);
//                return ResponseEntity.ok("User deleted successfully");
//            } else {
//                UserErrorResponse errorResponse = new UserErrorResponse();
//                errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
//                errorResponse.setMessage("Invalid password");
//                errorResponse.setTimeStamp(System.currentTimeMillis());
//
//                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
//            }
//        } else {
//            UserErrorResponse errorResponse = new UserErrorResponse();
//            errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
//            errorResponse.setMessage("User not found");
//            errorResponse.setTimeStamp(System.currentTimeMillis());
//
//            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
//        }
//    }
}
