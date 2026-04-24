package com.iusjc.weschedule.service;

import com.iusjc.weschedule.enums.StatutReservation;
import com.iusjc.weschedule.models.Reservation;
import com.iusjc.weschedule.models.Salle;
import com.iusjc.weschedule.models.SeanceClasse;
import com.iusjc.weschedule.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationSalleServiceTest {

    @Mock
    private SalleRepository salleRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private SeanceClasseRepository seanceRepository;
    @Mock
    private EquipmentRepository equipmentRepository;
    @Mock
    private ClasseRepository classeRepository;
    @Mock
    private EmploiDuTempsClasseRepository emploiDuTempsRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private CoursRepository coursRepository;

    @InjectMocks
    private ReservationSalleService reservationSalleService;

    @Test
    void estSalleDisponible_ShouldReturnTrue_WhenNoConflit() {
        // Arrange
        Salle salle = new Salle();
        salle.setIdSalle(UUID.randomUUID());
        LocalDate date = LocalDate.now();
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(10, 0);

        when(seanceRepository.existsBySalleAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                eq(salle), eq(date), eq(end), eq(start))).thenReturn(false);
        when(reservationRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        boolean result = reservationSalleService.estSalleDisponible(salle, date, start, end);

        // Assert
        assertTrue(result);
    }

    @Test
    void estSalleDisponible_ShouldReturnFalse_WhenSeanceConflit() {
        // Arrange
        Salle salle = new Salle();
        salle.setIdSalle(UUID.randomUUID());
        LocalDate date = LocalDate.now();
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(10, 0);

        when(seanceRepository.existsBySalleAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                eq(salle), eq(date), eq(end), eq(start))).thenReturn(true);

        // Act
        boolean result = reservationSalleService.estSalleDisponible(salle, date, start, end);

        // Assert
        assertFalse(result);
    }

    @Test
    void validerReservation_ShouldSucceed_WhenNoConflicts() {
        // Arrange
        UUID resId = UUID.randomUUID();
        Salle salle = new Salle();
        salle.setIdSalle(UUID.randomUUID());
        
        Reservation res = new Reservation();
        res.setIdResa(resId);
        res.setSalle(salle);
        res.setStartAt(LocalDateTime.now().plusDays(1));
        res.setEndAt(LocalDateTime.now().plusDays(1).plusHours(2));
        res.setStatut(StatutReservation.EN_ATTENTE);

        when(reservationRepository.findById(resId)).thenReturn(Optional.of(res));
        // Mock estSalleDisponible internal logic
        when(seanceRepository.existsBySalleAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.findAll()).thenReturn(Collections.singletonList(res));

        // Act
        reservationSalleService.validerReservation(resId);

        // Assert
        assertEquals(StatutReservation.CONFIRMEE, res.getStatut());
        verify(reservationRepository, times(1)).save(res);
    }

    @Test
    void validerReservation_ShouldThrowException_WhenConflictDetected() {
        // Arrange
        UUID resId = UUID.randomUUID();
        Salle salle = new Salle();
        salle.setIdSalle(UUID.randomUUID());
        
        Reservation res = new Reservation();
        res.setIdResa(resId);
        res.setSalle(salle);
        res.setStartAt(LocalDateTime.now().plusDays(1));
        res.setEndAt(LocalDateTime.now().plusDays(1).plusHours(2));

        when(reservationRepository.findById(resId)).thenReturn(Optional.of(res));
        // Simulate conflict in seance
        when(seanceRepository.existsBySalleAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(any(), any(), any(), any())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> reservationSalleService.validerReservation(resId));
    }

    @Test
    void annulerReservation_ShouldChangeStatut() {
        // Arrange
        UUID resId = UUID.randomUUID();
        Reservation res = new Reservation();
        res.setStatut(StatutReservation.CONFIRMEE);
        when(reservationRepository.findById(resId)).thenReturn(Optional.of(res));

        // Act
        reservationSalleService.annulerReservation(resId);

        // Assert
        assertEquals(StatutReservation.ANNULEE, res.getStatut());
        verify(reservationRepository).save(res);
    }
}
