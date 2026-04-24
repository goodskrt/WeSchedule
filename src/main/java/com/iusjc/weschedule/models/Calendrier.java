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
@Table(name = "calendriers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Calendrier {
    @Id
    @GeneratedValue
    private UUID idCalendrier;
    private String nomCalendrier;
    private String description;
}