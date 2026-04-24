package com.iusjc.weschedule.models;

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
    private String typeRessource;
    private Integer quantite;
}
