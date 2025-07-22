package com.example.auth.service;

import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository users, RoleRepository roles) {
        this.users = users;
        this.roles = roles;
    }

    public User register(String login, String email, String rawPassword) {
        if (users.existsByLogin(login)) throw new RuntimeException("Login exists");
        if (users.existsByEmail(email)) throw new RuntimeException("Email exists");

        User u = new User();
        u.setLogin(login);
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPassword));
        Role guest = roles.findByName("ROLE_GUEST")
                .orElseThrow(() -> new RuntimeException("ROLE_GUEST not found"));
        u.setRoles(Set.of(guest));
        return users.save(u);
    }

    public User findByLogin(String login) {
        return users.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
