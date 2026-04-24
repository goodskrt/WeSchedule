package com.iusjc.weschedule.models;

import com.iusjc.weschedule.enums.TypeNotification;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue
    private UUID idNotif;

    @Enumerated(EnumType.STRING)
    private TypeNotification typeNotif;

    private String contenu;

    private LocalDateTime dateEnvoi = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "etudiant_id")
    private Etudiant destinataire;
}