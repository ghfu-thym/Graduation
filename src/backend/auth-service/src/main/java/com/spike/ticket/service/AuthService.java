package com.spike.ticket.service;


import com.spike.ticket.dto.LoginRequest;
import com.spike.ticket.dto.RegisterRequest;
import com.spike.ticket.entity.User;
import com.spike.ticket.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(RegisterRequest request) {
        if(userRepo.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (userRepo.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encodedPassword)
                .build();

        userRepo.save(user);
        return "User registered successfully for user: " + user.getUsername();
    }

    public String login(LoginRequest request) {

        User user = userRepo.findByUsername(request.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("Invalid username or password")
        );

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("Invalid password");
        }
        return jwtService.generateToken(user);
    }
}
