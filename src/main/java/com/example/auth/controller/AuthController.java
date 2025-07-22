package com.example.auth.controller;

import com.example.auth.model.Role;
import com.example.auth.model.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtTokenUtil;
import com.example.auth.service.RefreshTokenService;
import com.example.auth.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtTokenUtil jwtUtil;
    private final RefreshTokenService refreshSvc;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder pwdEncoder;

    public AuthController(AuthenticationManager authManager,
                          JwtTokenUtil jwtUtil,
                          RefreshTokenService refreshSvc,
                          UserRepository userRepo,
                          RoleRepository roleRepo,
                          PasswordEncoder pwdEncoder) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.refreshSvc = refreshSvc;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.pwdEncoder = pwdEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body("Username is taken");
        }
        if (userRepo.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body("Email is in use");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(pwdEncoder.encode(req.getPassword()));
        Role guest = roleRepo.findByName("ROLE_GUEST")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.setRoles(Set.of(guest));
        userRepo.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        UserDetails ud = (UserDetails) auth.getPrincipal();

        String access = jwtUtil.generateAccessToken(ud);
        String refresh = jwtUtil.generateRefreshToken(ud);
        refreshSvc.createRefreshToken(ud.getUsername(), refresh);

        return ResponseEntity.ok(new JwtResponse(access, refresh));
    }

    @PostMapping("/refresh")
    public ResponseEntity refresh(@RequestBody TokenRefreshRequest req) {
        return refreshSvc.findByToken(req.getRefreshToken())
                .map(refreshSvc::verifyExpiration)
                .map(rt -> {
                    User u = rt.getUser();
                    UserDetails ud = org.springframework.security.core.userdetails.User
                            .withUsername(u.getUsername())
                            .password(u.getPassword())
                            .authorities(u.getRoles().stream()
                                    .map(r -> new SimpleGrantedAuthority(r.getName()))
                                    .collect(Collectors.toList()))
                            .build();
                    String newAccess = jwtUtil.generateAccessToken(ud);
                    return ResponseEntity.ok(new JwtResponse(newAccess, rt.getToken()));
                })
                .orElseGet(() -> ResponseEntity.status(401)
                        .body(new JwtResponse(null, null)));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody TokenRefreshRequest req) {
        int deleted = refreshSvc.deleteByToken(req.getRefreshToken());
        if (deleted > 0) {
            return ResponseEntity.ok("Logged out successfully");
        }
        return ResponseEntity.badRequest().body("Invalid token");
    }
}
