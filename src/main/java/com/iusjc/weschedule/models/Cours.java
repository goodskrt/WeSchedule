package com.iusjc.weschedule.models;

import com.iusjc.weschedule.enums.TypeCours;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cours",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"ue_id", "classe_id", "type_cours"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cours {

    @Id
    @GeneratedValue
    private UUID idCours;

    private String intitule;

    @Enumerated(EnumType.STRING)
    private TypeCours typeCours;

    /** Durée totale prévue pour ce cours (en heures) */
    private Integer dureeTotal;

    /** Durée d'une séance par jour (en heures) */
    private Integer dureeSeanceParJour;

    /** Heures restantes à planifier (décrémentées au fil des séances) */
    private Integer dureeRestante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ue_id")
    private UE ue;

    /** La classe concernée par ce cours */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id")
    private Classe classe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id")
    private Enseignant enseignant;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ── Helpers ──────────────────────────────────────────────────────────

    /** Heures déjà effectuées = total - restantes */
    @Transient
    public int getHeuresEffectuees() {
        if (dureeTotal == null || dureeRestante == null) return 0;
        return Math.max(0, dureeTotal - dureeRestante);
    }

    /** Pourcentage d'avancement (0-100) */
    @Transient
    public double getPourcentageAvancement() {
        if (dureeTotal == null || dureeTotal == 0) return 0.0;
        return (getHeuresEffectuees() * 100.0) / dureeTotal;
    }

    /** true si toutes les heures ont été effectuées */
    @Transient
    public boolean isTermine() {
        return dureeRestante != null && dureeRestante == 0;
    }
}
