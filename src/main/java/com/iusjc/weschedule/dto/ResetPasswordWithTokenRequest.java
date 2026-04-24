package com.iusjc.weschedule.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordWithTokenRequest {
    
    @NotBlank(message = "Le token ne peut pas être vide")
    private String token;
    
    @NotBlank(message = "Le mot de passe ne peut pas être vide")
    private String newPassword;
    
    @NotBlank(message = "La confirmation du mot de passe ne peut pas être vide")
    private String confirmPassword;
}
