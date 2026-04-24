package com.iusjc.weschedule.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "creneaux_disponibilite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"disponibilite", "plagesHoraires"})
public class CreneauDisponibilite {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disponibilite_id", nullable = false)
    private DisponibiliteEnseignant disponibilite;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @OneToMany(mappedBy = "creneauDisponibilite", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PlageHoraire> plagesHoraires;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CreneauDisponibilite)) return false;
        CreneauDisponibilite that = (CreneauDisponibilite) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
