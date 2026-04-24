package com.iusjc.weschedule.dto;

import com.iusjc.weschedule.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String idUser; // Changé de UUID à String pour compatibilité JSON/Frontend
    private String email;
    private String nom;
    private String prenom;
    private Role role;
    private String message;
    private boolean success;
    private String token; // Token JWT pour l'authentification
    private String phone; // Téléphone (pour les enseignants)
    private String grade; // Grade (pour les enseignants)
}
