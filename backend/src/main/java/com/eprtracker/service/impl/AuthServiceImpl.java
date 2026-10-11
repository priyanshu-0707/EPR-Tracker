package com.eprtracker.service.impl;

import com.eprtracker.dto.request.LoginRequest;
import com.eprtracker.dto.request.RegisterRequest;
import com.eprtracker.dto.response.AuthResponse;
import com.eprtracker.entity.Company;
import com.eprtracker.exception.ValidationException;
import com.eprtracker.repository.CompanyRepository;
import com.eprtracker.security.JwtTokenProvider;
import com.eprtracker.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider) {
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (companyRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ValidationException("An enterprise account already exists with this email address");
        }

        String companyId = UUID.randomUUID().toString();
        Company company = Company.builder()
                .companyId(companyId)
                .companyName(request.getCompanyName())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .state(request.getState())
                .cpcbRegistrationNumber(request.getCpcbRegistrationNumber() != null ? request.getCpcbRegistrationNumber() : "CPCB/EPR/" + companyId.substring(0, 8).toUpperCase())
                .registeredOn(Instant.now().toString())
                .alertsEnabled(true)
                .alertEmail(request.getEmail())
                .alertPhone("+919876543210")
                .build();

        companyRepository.save(company);
        String token = tokenProvider.generateToken(company.getCompanyId(), company.getEmail());

        return AuthResponse.builder()
                .token(token)
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .email(company.getEmail())
                .state(company.getState())
                .cpcbRegistrationNumber(company.getCpcbRegistrationNumber())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Company company = companyRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ValidationException("Invalid email address or credentials"));

        if (!passwordEncoder.matches(request.getPassword(), company.getPasswordHash())) {
            throw new ValidationException("Invalid email address or credentials");
        }

        String token = tokenProvider.generateToken(company.getCompanyId(), company.getEmail());

        return AuthResponse.builder()
                .token(token)
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .email(company.getEmail())
                .state(company.getState())
                .cpcbRegistrationNumber(company.getCpcbRegistrationNumber())
                .build();
    }

    @Override
    public AuthResponse refresh(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!tokenProvider.validateToken(token)) {
            throw new ValidationException("Invalid or expired session token");
        }

        String companyId = tokenProvider.getCompanyIdFromToken(token);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ValidationException("Company account no longer exists"));

        String newToken = tokenProvider.generateToken(company.getCompanyId(), company.getEmail());

        return AuthResponse.builder()
                .token(newToken)
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .email(company.getEmail())
                .state(company.getState())
                .cpcbRegistrationNumber(company.getCpcbRegistrationNumber())
                .build();
    }
}
