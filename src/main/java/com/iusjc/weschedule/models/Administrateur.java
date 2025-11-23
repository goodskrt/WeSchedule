package com.iusjc.weschedule.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "administrateurs")
@Data
public class Administrateur extends Utilisateur {
    // rien en plus pour l'instant
}