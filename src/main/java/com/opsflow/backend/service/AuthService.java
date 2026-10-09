package com.opsflow.backend.service;

import com.opsflow.backend.dto.AuthResponse;
import com.opsflow.backend.dto.LoginRequest;
import com.opsflow.backend.dto.RegisterRequest;
import com.opsflow.backend.entity.AppUser;
import com.opsflow.backend.entity.UserRole;
import com.opsflow.backend.exception.InvalidCredentialsException;
import com.opsflow.backend.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(
            RegisterRequest request) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        AppUser user =
                new AppUser();

        user.setName(
                request.getName().trim()
        );

        user.setEmail(email);

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setRole(
                UserRole.USER
        );

        AppUser savedUser =
                userRepository.save(user);

        String token =
                jwtService.generateToken(
                        savedUser
                );

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                token
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(
            LoginRequest request) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        AppUser user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "Invalid email or password"
                                )
                        );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        String token =
                jwtService.generateToken(user);

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                token
        );
    }
}