package com.iusjc.weschedule.models;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "enseignants")
@Data
public class Enseignant extends Utilisateur {
    private String grade;
    private String disponibilite;
    private String matiere;
}