package com.alper.worldcup.service;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {

    private final JdbcTemplate jdbcTemplate;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(JdbcTemplate jdbcTemplate,
                              UserDetailsService userDetailsService,
                              PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<String> findAdminUsernames() {
        return jdbcTemplate.queryForList(
                "SELECT username FROM authorities WHERE authority = 'ROLE_ADMIN' ORDER BY username",
                String.class);
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        validateNewPassword(newPassword);

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException ex) {
            throw new IllegalArgumentException("User not found");
        }

        if (!passwordEncoder.matches(currentPassword, userDetails.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (passwordEncoder.matches(newPassword, userDetails.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        jdbcTemplate.update(
                "UPDATE users SET password = ? WHERE username = ?",
                encodePassword(newPassword),
                username);
    }

    @Transactional
    public void ensureUser(String username, String password, boolean admin) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?",
                Integer.class,
                username);

        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO users (username, password, enabled) VALUES (?, ?, 1)",
                    username,
                    encodePassword(password));
        } else {
            jdbcTemplate.update(
                    "UPDATE users SET enabled = 1 WHERE username = ?",
                    username);
        }

        jdbcTemplate.update(
                "DELETE FROM authorities WHERE username = ? AND authority = 'ROLE_USER'",
                username);
        jdbcTemplate.update(
                "INSERT IGNORE INTO authorities (username, authority) VALUES (?, 'ROLE_USER')",
                username);

        if (admin) {
            jdbcTemplate.update(
                    "INSERT IGNORE INTO authorities (username, authority) VALUES (?, 'ROLE_ADMIN')",
                    username);
        } else {
            jdbcTemplate.update(
                    "DELETE FROM authorities WHERE username = ? AND authority = 'ROLE_ADMIN'",
                    username);
        }
    }

    private void validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.length() < 3) {
            throw new IllegalArgumentException("Password must be at least 3 characters");
        }
    }

    private String encodePassword(String rawPassword) {
        return "{noop}" + rawPassword;
    }
}
