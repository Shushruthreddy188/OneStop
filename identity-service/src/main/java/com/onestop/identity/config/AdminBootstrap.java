package com.onestop.identity.config;

import com.onestop.identity.domain.User;
import com.onestop.identity.repo.RoleRepository;
import com.onestop.identity.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Optional, environment-driven first-admin bootstrap; no credential is stored in source control. */
@Component
public class AdminBootstrap implements ApplicationRunner {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;

    public AdminBootstrap(UserRepository users, RoleRepository roles, PasswordEncoder encoder,
                          @Value("${onestop.bootstrap.admin-email:}") String email,
                          @Value("${onestop.bootstrap.admin-password:}") String password) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
        this.email = email == null ? "" : email.trim().toLowerCase();
        this.password = password == null ? "" : password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.length() < 12) {
            throw new IllegalStateException("ADMIN_EMAIL and ADMIN_PASSWORD (minimum 12 characters) must both be set");
        }
        var adminRole = roles.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN is missing"));
        User user = users.findByEmail(email).orElseGet(() -> {
            User created = new User();
            created.setEmail(email);
            created.setPasswordHash(encoder.encode(password));
            created.setFirstName("OneStop");
            created.setLastName("Admin");
            return created;
        });
        user.getRoles().add(adminRole);
        users.save(user);
    }
}
