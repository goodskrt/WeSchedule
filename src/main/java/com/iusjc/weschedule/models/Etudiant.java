package com.iusjc.weschedule.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "etudiants")
@Data
public class Etudiant extends Utilisateur {

    private String filiere;
    private String niveau;
    private String groupe; // ou une vraie relation plus tard
}