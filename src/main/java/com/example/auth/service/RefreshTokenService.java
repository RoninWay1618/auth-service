package com.example.auth.service;

import com.example.auth.model.RefreshToken;
import com.example.auth.model.User;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenService {
    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    private final RefreshTokenRepository tokenRepo;
    private final UserRepository userRepo;

    public RefreshTokenService(RefreshTokenRepository tokenRepo, UserRepository userRepo) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public RefreshToken createRefreshToken(String username, String tokenStr) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        tokenRepo.deleteByUser(user);

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(tokenStr);
        rt.setExpiryDate(Instant.now().plusMillis(refreshExpiration));
        return tokenRepo.save(rt);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return tokenRepo.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            tokenRepo.delete(token);
            throw new RuntimeException("Refresh token expired");
        }
        return token;
    }

    @Transactional
    public int deleteByToken(String token) {
        return tokenRepo.deleteByToken(token);
    }
}
