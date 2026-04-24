package com.iusjc.weschedule.service;

import com.iusjc.weschedule.enums.Role;
import com.iusjc.weschedule.models.Enseignant;
import com.iusjc.weschedule.models.UE;
import com.iusjc.weschedule.models.Utilisateur;
import com.iusjc.weschedule.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnseignantServiceTest {

    @Mock
    private EnseignantRepository enseignantRepository;
    @Mock
    private UERepository ueRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private DisponibiliteEnseignantRepository disponibiliteRepository;
    @Mock
    private CoursRepository coursRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private EnseignantService enseignantService;

    @Test
    void creerEnseignant_ShouldSucceed_WhenValid() {
        // Arrange
        String email = "teacher@test.com";
        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");
        when(enseignantRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        ReflectionTestUtils.setField(enseignantService, "baseUrl", "http://localhost");

        // Act
        Enseignant result = enseignantService.creerEnseignant("Nom", "Prenom", email, "1234567890", "Grade", null);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(Role.ENSEIGNANT, result.getRole());
        verify(emailService).sendEmail(eq(email), anyString(), anyString());
        verify(enseignantRepository).save(any());
    }

    @Test
    void creerEnseignant_ShouldThrowException_WhenEmailExists() {
        // Arrange
        String email = "exists@test.com";
        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(new Utilisateur()));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            enseignantService.creerEnseignant("Nom", "Prenom", email, "123", "G", null)
        );
    }

    @Test
    void updateEnseignant_ShouldUpdateFields() {
        // Arrange
        UUID id = UUID.randomUUID();
        Enseignant existing = new Enseignant();
        existing.setIdUser(id);
        existing.setEmail("old@test.com");
        
        when(enseignantRepository.findById(id)).thenReturn(Optional.of(existing));
        when(enseignantRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        Enseignant result = enseignantService.updateEnseignant(id, "NewNom", "NewPrenom", "new@test.com", "999", "NewGrade", Collections.emptyList());

        // Assert
        assertEquals("NewNom", result.getNom());
        assertEquals("new@test.com", result.getEmail());
        verify(enseignantRepository).save(existing);
    }

    @Test
    void changerMotDePasse_ShouldSucceed_WhenOldPasswordMatches() {
        // Arrange
        UUID id = UUID.randomUUID();
        Enseignant existing = new Enseignant();
        existing.setMotDePasse("old_hashed");
        
        when(enseignantRepository.findById(id)).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("old_plain", "old_hashed")).thenReturn(true);
        when(passwordEncoder.encode("new_plain")).thenReturn("new_hashed");

        // Act
        enseignantService.changerMotDePasse(id, "old_plain", "new_plain");

        // Assert
        assertEquals("new_hashed", existing.getMotDePasse());
        verify(enseignantRepository).save(existing);
    }

    @Test
    void changerMotDePasse_ShouldThrowException_WhenOldPasswordIncorrect() {
        // Arrange
        UUID id = UUID.randomUUID();
        Enseignant existing = new Enseignant();
        existing.setMotDePasse("old_hashed");
        
        when(enseignantRepository.findById(id)).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrong_plain", "old_hashed")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            enseignantService.changerMotDePasse(id, "wrong_plain", "new_plain")
        );
    }
}
