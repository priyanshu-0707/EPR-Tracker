package com.eprtracker.service;

import com.eprtracker.dto.request.LoginRequest;
import com.eprtracker.dto.request.RegisterRequest;
import com.eprtracker.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String token);
}
