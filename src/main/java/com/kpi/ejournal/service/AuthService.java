package com.kpi.ejournal.service;

import com.kpi.ejournal.dto.auth.AuthResponse;
import com.kpi.ejournal.dto.auth.LoginRequest;
import com.kpi.ejournal.entity.user.User;
import com.kpi.ejournal.exception.BadRequestException;
import com.kpi.ejournal.repository.user.UserRepository;
import com.kpi.ejournal.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByLogin(request.login())
                .orElseThrow(() -> new BadRequestException("Неправильний логін або пароль"));

        if (!encoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Неправильний логін або пароль");
        }

        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(
                user.getId(),
                user.getLogin(),
                user.getRole().name()
        );

        return new AuthResponse(
                token,
                user.getId(),
                user.getFullName(),
                user.getRole().name()
        );
    }

    public void logout() {}

    public String hash(String rawPassword)
    {
        return encoder.encode(rawPassword);
    }
}