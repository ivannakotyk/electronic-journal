package com.kpi.ejournal.controller;

import com.kpi.ejournal.dto.*;
import com.kpi.ejournal.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService)
    {
        this.authService = authService;
    }
    @PostMapping("/login")
    public AuthResponse
    login(@Valid @RequestBody LoginRequest request)
    {
        return authService.login(request);
    }
    @PostMapping("/logout")
    public void logout()
    {
        authService.logout();
    }
}
