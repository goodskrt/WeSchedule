package com.iusjc.weschedule.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "administrateurs")
@Data
@EqualsAndHashCode(callSuper = false)
public class Administrateur extends Utilisateur {
    // rien en plus pour l'instant
}