package com.example.auth.service;

import com.example.auth.entity.RefreshToken;
import com.example.auth.entity.User;
import com.example.auth.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository repo;
    private final long refreshExpMs;

    public RefreshTokenService(
            RefreshTokenRepository repo,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpMs
    ) {
        this.repo = repo;
        this.refreshExpMs = refreshExpMs;
    }

    public RefreshToken create(User user) {
        RefreshToken t = new RefreshToken();
        t.setId(UUID.randomUUID().toString());
        t.setUser(user);
        t.setCreatedAt(Instant.now());
        t.setExpiresAt(Instant.now().plusMillis(refreshExpMs));
        return repo.save(t);
    }

    public RefreshToken verify(String id) {
        return repo.findByIdAndExpiresAtAfter(id, Instant.now())
                .orElseThrow(() -> new RuntimeException("Invalid or expired refresh token"));
    }

    public void revokeByUser(User user) {
        repo.deleteByUserId(user.getId());
    }
}
