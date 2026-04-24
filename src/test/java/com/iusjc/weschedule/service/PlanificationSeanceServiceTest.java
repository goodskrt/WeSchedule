package com.iusjc.weschedule.service;

import com.iusjc.weschedule.enums.StatutUE;
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
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanificationSeanceServiceTest {

    @Mock
    private CoursRepository coursRepository;
    @Mock
    private ClasseRepository classeRepository;
    @Mock
    private EnseignantRepository enseignantRepository;
    @Mock
    private DisponibiliteEnseignantRepository disponibiliteRepository;
    @Mock
    private SeanceClasseRepository seanceRepository;
    @Mock
    private SalleRepository salleRepository;
    @Mock
    private EmploiDuTempsClasseRepository emploiDuTempsRepository;
    @Mock
    private DureeService dureeService;
    @Mock
    private ReservationSalleService reservationSalleService;

    @InjectMocks
    private PlanificationSeanceService planificationSeanceService;

    @Test
    void getCoursPlanifiables_ShouldFilterCorrectly() {
        // Arrange
        UUID classeId = UUID.randomUUID();
        Classe classe = new Classe();
        UE ue1 = new UE(); ue1.setStatut(StatutUE.ACTIF);
        UE ue2 = new UE(); ue2.setStatut(StatutUE.INACTIF);
        classe.setUes(Set.of(ue1, ue2));

        Cours cours1 = new Cours(); cours1.setUe(ue1);
        Cours cours2 = new Cours(); cours2.setUe(ue2);

        when(classeRepository.findById(classeId)).thenReturn(Optional.of(classe));
        when(coursRepository.findByUe(ue1)).thenReturn(Collections.singletonList(cours1));
        when(dureeService.aDesHeuresRestantes(cours1)).thenReturn(true);

        // Act
        var result = planificationSeanceService.getCoursPlanifiables(classeId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(cours1, result.get(0).getCours());
    }

    @Test
    void creerSeanceDepuisCreneau_ShouldThrowException_WhenNoHoursLeft() {
        // Arrange
        UUID edtId = UUID.randomUUID();
        UUID coursId = UUID.randomUUID();
        Cours cours = new Cours();
        UE ue = new UE(); ue.setStatut(StatutUE.ACTIF);
        cours.setUe(ue);

        when(emploiDuTempsRepository.findById(edtId)).thenReturn(Optional.of(new EmploiDuTempsClasse()));
        when(coursRepository.findById(coursId)).thenReturn(Optional.of(cours));
        when(dureeService.aDesHeuresRestantes(cours)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            planificationSeanceService.creerSeanceDepuisCreneau(edtId, coursId, UUID.randomUUID(), null, LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(1), "")
        );
    }

    @Test
    void creerSeanceDepuisCreneau_ShouldSucceed_WhenValid() {
        // Arrange
        UUID edtId = UUID.randomUUID();
        UUID coursId = UUID.randomUUID();
        UUID ensId = UUID.randomUUID();
        
        EmploiDuTempsClasse edt = new EmploiDuTempsClasse();
        edt.setClasse(new Classe());
        edt.getClasse().setIdClasse(UUID.randomUUID());

        Cours cours = new Cours();
        UE ue = new UE(); ue.setStatut(StatutUE.ACTIF);
        cours.setUe(ue);

        Enseignant ens = new Enseignant();
        ens.setIdUser(ensId);

        when(emploiDuTempsRepository.findById(edtId)).thenReturn(Optional.of(edt));
        when(coursRepository.findById(coursId)).thenReturn(Optional.of(cours));
        when(enseignantRepository.findById(ensId)).thenReturn(Optional.of(ens));
        when(dureeService.aDesHeuresRestantes(cours)).thenReturn(true);
        
        // Mock availability (simplified - it's private but we can mock repository it uses)
        when(disponibiliteRepository.findByEnseignantAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(any(), any(), any()))
                .thenReturn(Collections.singletonList(new DisponibiliteEnseignant()));
        // Note: The logic inside estDisponible needs more mocking for a full pass, 
        // but let's see if we can get past the first checks.
        // Actually, estDisponible will return false if CreneauDisponibilite list is empty.
        
        DisponibiliteEnseignant dispo = new DisponibiliteEnseignant();
        CreneauDisponibilite creneau = new CreneauDisponibilite();
        creneau.setDate(LocalDate.now());
        PlageHoraire plage = new PlageHoraire();
        plage.setHeureDebut(LocalTime.of(8, 0));
        plage.setHeureFin(LocalTime.of(12, 0));
        creneau.setPlagesHoraires(Set.of(plage));
        dispo.setCreneauxParJour(Set.of(creneau));
        
        when(disponibiliteRepository.findByEnseignantAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(any(), any(), any()))
                .thenReturn(Collections.singletonList(dispo));
        when(seanceRepository.findByEnseignantAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(seanceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        SeanceClasse result = planificationSeanceService.creerSeanceDepuisCreneau(
                edtId, coursId, ensId, null, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), "Test");

        // Assert
        assertNotNull(result);
        assertEquals(ens, result.getEnseignant());
        verify(seanceRepository).save(any());
        verify(dureeService).decrementerApresSeance(any());
    }
}
