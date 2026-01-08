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

    private UUID idUser;
    private String email;
    private String nom;
    private String prenom;
    private Role role;
    private String message;
    private boolean success;
}
