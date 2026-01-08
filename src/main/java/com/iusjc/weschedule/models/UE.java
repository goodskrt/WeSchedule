package com.iusjc.weschedule.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "ues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"classes"})
@ToString(exclude = {"classes"})
public class UE {
    @Id
    @GeneratedValue
    private UUID idUE;
    
    private String intitule;
    private String code;
    private Integer duree;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "ue_classe",
        joinColumns = @JoinColumn(name = "ue_id"),
        inverseJoinColumns = @JoinColumn(name = "classe_id")
    )
    private Set<Classe> classes;
}