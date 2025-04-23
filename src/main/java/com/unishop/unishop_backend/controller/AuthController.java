package com.unishop.unishop_backend.controller;

import com.unishop.unishop_backend.entity.User;
import com.unishop.unishop_backend.repository.UserRepository;
import com.unishop.unishop_backend.utils.JwtUtil;
import com.unishop.unishop_backend.exception.user.UserErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
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

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@CookieValue(name = "token", required = false) String token) {
        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractUsername(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized"));
        }
        Optional<User> userOptional = userRepository.findByUsername(jwtUtil.extractUsername(token));

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return ResponseEntity.ok(Map.of(
                    "username", user.getUsername(),
                    "name", user.getName(),
                    "role", user.getRole()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized"));
        }

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpServletResponse response) {
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

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Strict")
                .secure(true)
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("token", token));
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

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .secure(true)
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Logged out successfully"));
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
