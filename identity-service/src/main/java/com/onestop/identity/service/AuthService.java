package com.onestop.identity.service;

import com.onestop.identity.domain.Role;
import com.onestop.identity.domain.User;
import com.onestop.identity.error.ApiExceptions.EmailAlreadyUsedException;
import com.onestop.identity.error.ApiExceptions.InvalidCredentialsException;
import com.onestop.identity.repo.RoleRepository;
import com.onestop.identity.repo.UserRepository;
import com.onestop.identity.security.JwtService;
import com.onestop.identity.web.DtoMapper;
import com.onestop.identity.web.dto.AuthDtos.AuthResponse;
import com.onestop.identity.web.dto.AuthDtos.LoginRequest;
import com.onestop.identity.web.dto.AuthDtos.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE = "ROLE_CUSTOMER";

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, RoleRepository roles,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }

        Role customerRole = roles.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Missing role " + DEFAULT_ROLE));

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setFirstName(blankToNull(req.firstName()));
        user.setLastName(blankToNull(req.lastName()));
        user.setPhone(blankToNull(req.phone()));
        user.setStatus("ACTIVE");
        user.setRoles(Set.of(customerRole));

        users.save(user);
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        User user = users.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, "Bearer", jwtService.getTtlSeconds(), DtoMapper.toUserDto(user));
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
