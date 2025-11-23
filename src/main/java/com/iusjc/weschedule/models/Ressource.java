package com.iusjc.weschedule.models;

import com.iusjc.weschedule.enums.TypeRessource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "ressources")
@Data @NoArgsConstructor
@AllArgsConstructor
public class Ressource {
    @Id @GeneratedValue private UUID idRessource;
    @Enumerated(EnumType.STRING) private TypeRessource typeRessource;
    private Integer quantite;
}