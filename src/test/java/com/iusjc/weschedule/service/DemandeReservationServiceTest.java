package com.iusjc.weschedule.service;

import com.iusjc.weschedule.enums.Role;
import com.iusjc.weschedule.enums.StatutDemandeReservation;
import com.iusjc.weschedule.enums.StatutEquipement;
import com.iusjc.weschedule.models.Administrateur;
import com.iusjc.weschedule.models.DemandeReservationSalle;
import com.iusjc.weschedule.models.Enseignant;
import com.iusjc.weschedule.models.Equipment;
import com.iusjc.weschedule.models.Salle;
import com.iusjc.weschedule.repositories.DemandeReservationSalleRepository;
import com.iusjc.weschedule.repositories.EquipmentRepository;
import com.iusjc.weschedule.repositories.SalleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemandeReservationServiceTest {

    @Mock
    private DemandeReservationSalleRepository demandeRepository;
    @Mock
    private SalleRepository salleRepository;
    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private DemandeReservationService demandeReservationService;

    @Test
    void creerDemande_ShouldSucceed_WhenValid() {
        // Arrange
        Enseignant enseignant = new Enseignant();
        enseignant.setRole(Role.ENSEIGNANT);
        
        UUID salleId = UUID.randomUUID();
        Salle salle = new Salle();
        salle.setIdSalle(salleId);
        
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        
        when(salleRepository.findById(salleId)).thenReturn(Optional.of(salle));
        when(demandeRepository.existsSalleCreneauConflict(any(), any(), any(), any(), any())).thenReturn(false);
        when(demandeRepository.save(any(DemandeReservationSalle.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        DemandeReservationSalle result = demandeReservationService.creerDemande(
                enseignant, salleId, start, end, "Test Motif", null);

        // Assert
        assertNotNull(result);
        assertEquals(StatutDemandeReservation.EN_ATTENTE, result.getStatut());
        assertEquals(enseignant, result.getEnseignant());
        assertEquals(salle, result.getSalle());
        verify(demandeRepository).save(any(DemandeReservationSalle.class));
    }

    @Test
    void creerDemande_ShouldThrowException_WhenRoleIsInvalid() {
        // Arrange
        Administrateur admin = new Administrateur();
        admin.setRole(Role.ADMINISTRATEUR);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            demandeReservationService.creerDemande(admin, UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Motif", null)
        );
    }

    @Test
    void accepterDemande_ShouldSucceed_WhenValid() {
        // Arrange
        UUID demandeId = UUID.randomUUID();
        Administrateur admin = new Administrateur();
        admin.setRole(Role.ADMINISTRATEUR);
        
        DemandeReservationSalle demande = new DemandeReservationSalle();
        demande.setId(demandeId);
        demande.setStatut(StatutDemandeReservation.EN_ATTENTE);
        demande.setSalle(new Salle());
        demande.getSalle().setIdSalle(UUID.randomUUID());
        demande.setStartAt(LocalDateTime.now());
        demande.setEndAt(LocalDateTime.now().plusHours(1));
        demande.setEquipements(Collections.emptySet());

        when(demandeRepository.findById(demandeId)).thenReturn(Optional.of(demande));
        when(demandeRepository.existsSalleCreneauConflict(any(), any(), any(), any(), any())).thenReturn(false);

        // Act
        demandeReservationService.accepterDemande(demandeId, admin, "admin@test.com");

        // Assert
        assertEquals(StatutDemandeReservation.ACCEPTEE, demande.getStatut());
        verify(demandeRepository).save(demande);
    }

    @Test
    void refuserDemande_ShouldFail_WithoutCommentaire() {
        // Arrange
        UUID demandeId = UUID.randomUUID();
        Administrateur admin = new Administrateur();
        admin.setRole(Role.ADMINISTRATEUR);
        
        DemandeReservationSalle demande = new DemandeReservationSalle();
        demande.setStatut(StatutDemandeReservation.EN_ATTENTE);
        when(demandeRepository.findById(demandeId)).thenReturn(Optional.of(demande));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            demandeReservationService.refuserDemande(demandeId, admin, "admin@test.com", "")
        );
    }

    @Test
    void annulerParEnseignant_ShouldFail_IfEnseignantIsNotOwner() {
        // Arrange
        UUID demandeId = UUID.randomUUID();
        Enseignant owner = new Enseignant();
        owner.setIdUser(UUID.randomUUID());
        owner.setRole(Role.ENSEIGNANT);
        
        Enseignant other = new Enseignant();
        other.setIdUser(UUID.randomUUID());
        other.setRole(Role.ENSEIGNANT);
        
        DemandeReservationSalle demande = new DemandeReservationSalle();
        demande.setEnseignant(owner);
        demande.setStatut(StatutDemandeReservation.EN_ATTENTE);
        
        when(demandeRepository.findById(demandeId)).thenReturn(Optional.of(demande));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            demandeReservationService.annulerParEnseignant(demandeId, other)
        );
    }
}
