package com.iusjc.weschedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClasseResponse {

    private UUID id;
    private String nom;
    private String niveau;
    private UUID ecoleId;
    private String ecoleNom;
    private UUID filiereId;
    private String filiereNom;
    private Integer effectif;
    private Integer semestre;
    private Integer effectifMax;
    private String responsable;
    private String description;
    private String specialite;
    private List<UESimpleResponse> ues;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UESimpleResponse {
        private UUID id;
        private String code;
        private String nom;
    }
}
