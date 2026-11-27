package com.chanchal.employeemanagement.config;

import com.chanchal.employeemanagement.entity.AppUser;
import com.chanchal.employeemanagement.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@Configuration
public class SecurityDataInitializer {

    @Bean
    CommandLineRunner initializeUsers(
            AppUserRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${APP_SEED_USER_PASSWORD:}") String userPassword,
            @Value("${APP_SEED_ADMIN_PASSWORD:}") String adminPassword) {

        return args -> {
            if (StringUtils.hasText(userPassword) && !repository.existsByUsername("rahul")) {
                AppUser rahul = new AppUser();
                rahul.setUsername("rahul");
                rahul.setPassword(
                        passwordEncoder.encode(userPassword)
                );
                rahul.setRole("USER");
                rahul.setEnabled(true);

                repository.save(rahul);
            }

            if (StringUtils.hasText(adminPassword) && !repository.existsByUsername("admin")) {
                AppUser admin = new AppUser();
                admin.setUsername("admin");
                admin.setPassword(
                        passwordEncoder.encode(adminPassword)
                );
                admin.setRole("ADMIN");
                admin.setEnabled(true);

                repository.save(admin);
            }
        };
    }
}
