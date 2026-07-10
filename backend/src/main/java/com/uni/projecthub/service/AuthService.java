package com.uni.projecthub.service;

import com.uni.projecthub.dto.request.LoginRequest;
import com.uni.projecthub.dto.request.RegisterRequest;
import com.uni.projecthub.dto.response.AuthResponse;
import com.uni.projecthub.entity.User;
import com.uni.projecthub.enums.Role;
import com.uni.projecthub.repository.UserRepository;
import com.uni.projecthub.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Detect role from email domain
        // professors: @finki.ukim.mk
        // students:   @students.finki.ukim.mk
        String email = request.getEmail().toLowerCase();
        Role role;
        if (email.endsWith("@students.finki.ukim.mk")) {
            role = Role.STUDENT;
        } else if (email.endsWith("@finki.ukim.mk")) {
            role = Role.PROFESSOR;
        } else {
            throw new IllegalArgumentException(
                "Only @finki.ukim.mk and @students.finki.ukim.mk emails are allowed"
            );
        }

        // Student-only validations
        if (role == Role.STUDENT) {
            String index = request.getStudentIndex();
            if (index == null || index.isBlank()) {
                throw new IllegalArgumentException("Student index is required");
            }
            if (userRepository.existsByStudentIndex(index)) {
                throw new IllegalArgumentException("Student index " + index + " is already registered");
            }
            int currentYear = java.time.Year.now().getValue() % 100;
            int indexYear   = Integer.parseInt(index.substring(0, 2));
            if (indexYear > currentYear) {
                throw new IllegalArgumentException(
                    "Student index year prefix '" + String.format("%02d", indexYear) +
                    "' is in the future — please check your index"
                );
            }
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .studentIndex(role == Role.STUDENT ? request.getStudentIndex() : null)
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user);
        return buildResponse(user, token);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtUtil.generateToken(user);
        return buildResponse(user, token);
    }

    private AuthResponse buildResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .userId(user.getId())
                .studentIndex(user.getStudentIndex())
                .build();
    }
}
