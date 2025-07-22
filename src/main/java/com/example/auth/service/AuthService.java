package com.example.auth.service;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.RefreshTokenRequest;
import com.example.auth.dto.TokenResponse;
import com.example.auth.entity.RefreshToken;
import com.example.auth.entity.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AuthService {
    private final AuthenticationManager authManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshService;

    public AuthService(
            AuthenticationManager authManager,
            UserService userService,
            JwtService jwtService,
            RefreshTokenService refreshService
    ) {
        this.authManager = authManager;
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshService = refreshService;
    }

    public TokenResponse register(RegisterRequest req) {
        User u = userService.register(req.getLogin(), req.getEmail(), req.getPassword());
        return generateTokens(u);
    }

    public TokenResponse login(LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getLogin(), req.getPassword())
        );
        User u = userService.findByLogin(req.getLogin());
        return generateTokens(u);
    }

    public TokenResponse refresh(RefreshTokenRequest req) {
        RefreshToken rt = refreshService.verify(req.getRefreshToken());
        User u = rt.getUser();
        // Optionally: refreshService.revokeByUser(u); // одноразовый
        return generateTokens(u);
    }

    public void logout(RefreshTokenRequest req) {
        RefreshToken rt = refreshService.verify(req.getRefreshToken());
        refreshService.revokeByUser(rt.getUser());
    }

    private TokenResponse generateTokens(User u) {
        var roles = u.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toList());
        String access = jwtService.generateToken(u.getLogin(), roles);
        RefreshToken rt = refreshService.create(u);
        //return new TokenResponse(access, rt.getId());
        return new TokenResponse(access, rt.getId());
    }
}
