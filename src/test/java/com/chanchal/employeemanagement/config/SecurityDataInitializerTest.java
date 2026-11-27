package com.chanchal.employeemanagement.config;

import com.chanchal.employeemanagement.entity.AppUser;
import com.chanchal.employeemanagement.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SecurityDataInitializerTest {
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final SecurityDataInitializer initializer = new SecurityDataInitializer();

    @Test
    void skipsSeedingWhenPasswordsAreAbsentOrBlank() throws Exception {
        initializer.initializeUsers(users, encoder, "", "  ").run();
        verifyNoInteractions(users, encoder);
    }

    @Test
    void leavesExistingUsersAndPasswordsUntouched() throws Exception {
        when(users.existsByUsername("rahul")).thenReturn(true);
        when(users.existsByUsername("admin")).thenReturn(true);
        initializer.initializeUsers(users, encoder, UUID.randomUUID().toString(),
                UUID.randomUUID().toString()).run();
        verify(users, never()).save(any());
        verifyNoInteractions(encoder);
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "ADMIN"})
    void createsOnlyTheRequestedAccountWithAnEncodedPassword(String role) throws Exception {
        boolean admin = role.equals("ADMIN");
        String password = UUID.randomUUID().toString();
        when(encoder.encode(password)).thenReturn("encoded-test-value");
        initializer.initializeUsers(users, encoder, admin ? "" : password,
                admin ? password : "").run();
        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(users).save(saved.capture());
        AppUser user = saved.getValue();
        assertThat(user.getUsername()).isEqualTo(admin ? "admin" : "rahul");
        assertThat(user.getRole()).isEqualTo(role);
        assertThat(user.getPassword()).isEqualTo("encoded-test-value");
        assertThat(user.isEnabled()).isTrue();
    }
}
