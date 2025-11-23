package com.iusjc.weschedule.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "ecoles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ecole {

    @Id
    @GeneratedValue
    private UUID idEcole;

    private String nomEcole;
    private String adresse;
    private String telephone;
    private String email;
}