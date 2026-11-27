package org.example.resourcebookingappbackend.Configuration;

import org.example.resourcebookingappbackend.Enum.Role;
import org.example.resourcebookingappbackend.Model.Account;
import org.example.resourcebookingappbackend.Repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    public AdminSeeder(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        if (adminUsername == null || adminUsername.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.warn("ADMIN_USERNAME/ADMIN_PASSWORD are not configured - skipping admin seeding");
            return;
        }

        if (accountRepository.existsUsersByUserName(adminUsername)) {
            return;
        }

        Account admin = Account.builder()
                .userName(adminUsername)
                .fullName("System Administrator")
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .authProvider("LOCAL")
                .emailVerified(true)
                .build();

        accountRepository.save(admin);

        log.info("Seeded administrator account: {}", adminUsername);
    }
}