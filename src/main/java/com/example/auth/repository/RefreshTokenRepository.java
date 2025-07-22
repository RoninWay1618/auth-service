package com.example.auth.repository;

import com.example.auth.model.RefreshToken;
import com.example.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("delete from RefreshToken rt where rt.token = ?1")
    int deleteByToken(String token);

    @Modifying
    @Query("delete from RefreshToken rt where rt.user = ?1")
    int deleteByUser(User user);
}
