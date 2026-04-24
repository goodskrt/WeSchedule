package com.iusjc.weschedule.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "plages_horaires")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "creneauDisponibilite")
public class PlageHoraire {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creneau_disponibilite_id", nullable = false)
    private CreneauDisponibilite creneauDisponibilite;
    
    @Column(nullable = false)
    private LocalTime heureDebut;
    
    @Column(nullable = false)
    private LocalTime heureFin;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlageHoraire)) return false;
        PlageHoraire that = (PlageHoraire) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
