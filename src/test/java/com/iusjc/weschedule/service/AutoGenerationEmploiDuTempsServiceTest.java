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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutoGenerationEmploiDuTempsServiceTest {

    @Mock
    private EmploiDuTempsClasseRepository emploiDuTempsRepository;
    @Mock
    private SeanceClasseRepository seanceRepository;
    @Mock
    private CoursRepository coursRepository;
    @Mock
    private DisponibiliteEnseignantRepository disponibiliteRepository;
    @Mock
    private SalleRepository salleRepository;
    @Mock
    private ReservationSalleService reservationSalleService;

    @InjectMocks
    private AutoGenerationEmploiDuTempsService autoGenerationService;

    @Test
    void genererEmploiDuTemps_ShouldFail_WhenEmploiDuTempsNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(emploiDuTempsRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Map<String, Object> result = autoGenerationService.genererEmploiDuTemps(id, 1);

        // Assert
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("non trouvé"));
    }

    @Test
    void genererEmploiDuTemps_ShouldSucceed_BasicFlow() {
        // Arrange
        UUID id = UUID.randomUUID();
        Classe classe = new Classe();
        classe.setIdClasse(UUID.randomUUID());
        classe.setNom("ING4");
        classe.setEffectif(30);

        EmploiDuTempsClasse edt = new EmploiDuTempsClasse();
        edt.setId(id);
        edt.setClasse(classe);
        edt.setDateDebut(LocalDate.now().with(java.time.DayOfWeek.MONDAY));

        when(emploiDuTempsRepository.findById(id)).thenReturn(Optional.of(edt));
        
        // Mock candidates generation (empty to simplify)
        when(coursRepository.findByClasse(classe)).thenReturn(Collections.emptyList());
        
        // Mock TPE filling
        when(seanceRepository.findByEmploiDuTempsAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> result = autoGenerationService.genererEmploiDuTemps(id, 1);

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals(0, result.get("seancesAjoutees"));
    }

    @Test
    void remplirTrousAvecTPE_ShouldAddSeances() {
        // Arrange
        Classe classe = new Classe();
        classe.setIdClasse(UUID.randomUUID());
        
        EmploiDuTempsClasse edt = new EmploiDuTempsClasse();
        edt.setClasse(classe);
        edt.setDateDebut(LocalDate.now().with(java.time.DayOfWeek.MONDAY));

        // Mock TPE course creation/retrieval
        when(coursRepository.findByClasse(classe)).thenReturn(Collections.emptyList());
        
        // Mock all slots as empty
        when(seanceRepository.findByEmploiDuTempsAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        // Use reflection to call private method or test via genererEmploiDuTemps
        Map<String, Object> result = autoGenerationService.genererEmploiDuTemps(UUID.randomUUID(), 1);
        
        // The service logic for TPE: 8h to 17h (9 hours), 6 days (Mon-Sat).
        // Except lunch break (12h-13h) for 5 days.
        // Total slots: 9 * 6 - 5 = 54 - 5 = 49 slots.
        
        // We need to fix the arrange to pass the findById check in genererEmploiDuTemps
        when(emploiDuTempsRepository.findById(any())).thenReturn(Optional.of(edt));

        // Act again
        result = autoGenerationService.genererEmploiDuTemps(UUID.randomUUID(), 1);

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertTrue((Integer) result.get("seancesTPE") > 0);
    }
}
