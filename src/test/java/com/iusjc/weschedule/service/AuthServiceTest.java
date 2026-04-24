package com.iusjc.weschedule.service;

import com.iusjc.weschedule.dto.AuthResponse;
import com.iusjc.weschedule.dto.RegisterRequest;
import com.iusjc.weschedule.enums.Role;
import com.iusjc.weschedule.models.Administrateur;
import com.iusjc.weschedule.models.Enseignant;
import com.iusjc.weschedule.models.Utilisateur;
import com.iusjc.weschedule.repositories.AdministrateurRepository;
import com.iusjc.weschedule.repositories.EnseignantRepository;
import com.iusjc.weschedule.repositories.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private AdministrateurRepository administrateurRepository;

    @Mock
    private EnseignantRepository enseignantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldFail_WhenEmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        when(utilisateurRepository.findByEmail(anyString())).thenReturn(Optional.of(new Administrateur()));

        // Act
        AuthResponse response = authService.register(request, Role.ADMINISTRATEUR);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Cet email est déjà utilisé", response.getMessage());
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void register_ShouldSucceed_ForAdmin() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setNom("Doe");
        request.setPrenom("John");
        request.setEmail("john@doe.com");
        request.setMotDePasse("password");
        request.setPhone("1234567890");
        
        when(utilisateurRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        
        // Mock save to set an ID (since AuthService uses idUser.toString())
        doAnswer(invocation -> {
            Administrateur admin = invocation.getArgument(0);
            admin.setIdUser(UUID.randomUUID());
            return admin;
        }).when(administrateurRepository).save(any(Administrateur.class));

        // Act
        AuthResponse response = authService.register(request, Role.ADMINISTRATEUR);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Inscription réussie", response.getMessage());
        assertEquals("john@doe.com", response.getEmail());
        assertEquals(Role.ADMINISTRATEUR, response.getRole());
        verify(administrateurRepository, times(1)).save(any(Administrateur.class));
    }

    @Test
    void register_ShouldSucceed_ForEnseignant() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setNom("Smith");
        request.setPrenom("Jane");
        request.setEmail("jane@smith.com");
        request.setMotDePasse("password");
        request.setPhone("0987654321");
        
        when(utilisateurRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        
        doAnswer(invocation -> {
            Enseignant enseignant = invocation.getArgument(0);
            enseignant.setIdUser(UUID.randomUUID());
            return enseignant;
        }).when(enseignantRepository).save(any(Enseignant.class));

        // Act
        AuthResponse response = authService.register(request, Role.ENSEIGNANT);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Inscription réussie", response.getMessage());
        assertEquals(Role.ENSEIGNANT, response.getRole());
        verify(enseignantRepository, times(1)).save(any(Enseignant.class));
    }

    @Test
    void authenticate_ShouldSucceed_WhenCredentialsAreCorrect() {
        // Arrange
        String email = "john@doe.com";
        String password = "password";
        String hashed = "hashed_password";
        
        Administrateur user = new Administrateur();
        user.setIdUser(UUID.randomUUID());
        user.setEmail(email);
        user.setMotDePasse(hashed);
        user.setNom("John");
        user.setPrenom("Doe");
        user.setRole(Role.ADMINISTRATEUR);

        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, hashed)).thenReturn(true);

        // Act
        AuthResponse response = authService.authenticate(email, password);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Authentification réussie", response.getMessage());
        assertEquals(email, response.getEmail());
        assertEquals(user.getIdUser().toString(), response.getIdUser());
    }

    @Test
    void authenticate_ShouldFail_WhenUserNotFound() {
        // Arrange
        when(utilisateurRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        AuthResponse response = authService.authenticate("wrong@email.com", "password");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Email ou mot de passe incorrect", response.getMessage());
    }

    @Test
    void authenticate_ShouldFail_WhenPasswordIncorrect() {
        // Arrange
        Administrateur user = new Administrateur();
        user.setMotDePasse("hashed");
        when(utilisateurRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act
        AuthResponse response = authService.authenticate("test@test.com", "wrong_password");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Email ou mot de passe incorrect", response.getMessage());
    }
}
