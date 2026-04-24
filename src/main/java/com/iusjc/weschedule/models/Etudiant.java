package com.iusjc.weschedule.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Étudiant : fiche métier uniquement (pas de compte de connexion).
 */
@Entity
@Table(name = "etudiants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "idEtudiant")
@ToString(exclude = {"classe", "groupe"})
public class Etudiant {

    @Id
    @GeneratedValue
    @Column(name = "id_etudiant")
    private UUID idEtudiant;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    /** Email de contact facultatif (pas utilisé pour l'authentification). */
    @Column(unique = true)
    private String email;

    private String telephone;

    /** Numéro / matricule interne (facultatif mais unique s'il est renseigné). */
    @Column(unique = true)
    private String numeroEtudiant;

    private LocalDate dateNaissance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id")
    private Classe classe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupe_id")
    private Groupe groupe;
}
