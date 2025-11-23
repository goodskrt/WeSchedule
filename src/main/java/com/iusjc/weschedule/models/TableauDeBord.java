package com.iusjc.weschedule.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tableauxdebord")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableauDeBord {
    @Id
    @GeneratedValue
    private UUID idTableau;
    private String statistiques;
    private LocalDateTime dateCreation = LocalDateTime.now();
    private String contenu;
}