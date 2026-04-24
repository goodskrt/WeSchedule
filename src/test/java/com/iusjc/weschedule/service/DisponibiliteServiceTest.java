package com.iusjc.weschedule.service;

import com.iusjc.weschedule.models.*;
import com.iusjc.weschedule.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisponibiliteServiceTest {

    @Mock
    private DisponibiliteEnseignantRepository disponibiliteRepo;
    @Mock
    private CreneauDisponibiliteRepository creneauDispoRepo;
    @Mock
    private PlageHoraireRepository plageHoraireRepo;

    @InjectMocks
    private DisponibiliteService disponibiliteService;

    @Test
    void creerDisponibiliteSemaine_ShouldSucceed_WhenNoConflict() {
        // Arrange
        Enseignant ens = new Enseignant();
        ens.setIdUser(UUID.randomUUID());
        LocalDate date = LocalDate.of(2024, 4, 24); // Wednesday
        
        when(disponibiliteRepo.findByEnseignantAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(disponibiliteRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        DisponibiliteEnseignant result = disponibiliteService.creerDisponibiliteSemaine(ens, date);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.of(2024, 4, 22), result.getDateDebut()); // Monday
        assertEquals(LocalDate.of(2024, 4, 28), result.getDateFin());   // Sunday
        verify(disponibiliteRepo).save(any());
    }

    @Test
    void creerDisponibiliteSemaine_ShouldThrowException_WhenConflict() {
        // Arrange
        Enseignant ens = new Enseignant();
        when(disponibiliteRepo.findByEnseignantAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(any(), any(), any()))
                .thenReturn(Collections.singletonList(new DisponibiliteEnseignant()));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            disponibiliteService.creerDisponibiliteSemaine(ens, LocalDate.now())
        );
    }

    @Test
    void ajouterCreneau_ShouldSucceed_WhenValid() {
        // Arrange
        UUID dispoId = UUID.randomUUID();
        DisponibiliteEnseignant dispo = new DisponibiliteEnseignant();
        dispo.setDateDebut(LocalDate.now().minusDays(2));
        dispo.setDateFin(LocalDate.now().plusDays(2));
        
        when(disponibiliteRepo.findById(dispoId)).thenReturn(Optional.of(dispo));
        when(creneauDispoRepo.findByDisponibiliteAndDate(any(), any())).thenReturn(Collections.emptyList());
        when(creneauDispoRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(plageHoraireRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        PlageHoraire result = disponibiliteService.ajouterCreneau(dispoId, LocalDate.now(), LocalTime.of(8, 0), LocalTime.of(10, 0));

        // Assert
        assertNotNull(result);
        assertEquals(LocalTime.of(8, 0), result.getHeureDebut());
        verify(plageHoraireRepo).save(any());
    }

    @Test
    void ajouterCreneau_ShouldThrowException_WhenDateOutOfRange() {
        // Arrange
        UUID dispoId = UUID.randomUUID();
        DisponibiliteEnseignant dispo = new DisponibiliteEnseignant();
        dispo.setDateDebut(LocalDate.now().plusDays(1));
        dispo.setDateFin(LocalDate.now().plusDays(7));
        
        when(disponibiliteRepo.findById(dispoId)).thenReturn(Optional.of(dispo));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            disponibiliteService.ajouterCreneau(dispoId, LocalDate.now(), LocalTime.of(8, 0), LocalTime.of(10, 0))
        );
    }
}
