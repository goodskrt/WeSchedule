package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.dto.ApiResponse;
import com.iusjc.weschedule.dto.ClasseResponse;
import com.iusjc.weschedule.dto.CreateClasseRequest;
import com.iusjc.weschedule.dto.UpdateClasseRequest;
import com.iusjc.weschedule.service.ClasseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/classes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')")
public class ClasseController {

    private final ClasseService classeService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClasseResponse>> createClasse(
            @Valid @RequestBody CreateClasseRequest request) {
        ClasseResponse response = classeService.createClasse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Classe créée avec succès"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClasseResponse>>> getAllClasses() {
        List<ClasseResponse> classes = classeService.getAllClasses();
        return ResponseEntity.ok(
                ApiResponse.success(classes, "Classes récupérées avec succès"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClasseResponse>> getClasseById(@PathVariable UUID id) {
        ClasseResponse response = classeService.getClasseById(id);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Classe récupérée avec succès"));
    }

    @GetMapping("/ecole/{ecoleId}")
    public ResponseEntity<ApiResponse<List<ClasseResponse>>> getClassesByEcole(
            @PathVariable UUID ecoleId) {
        List<ClasseResponse> classes = classeService.getClassesByEcole(ecoleId);
        return ResponseEntity.ok(
                ApiResponse.success(classes, "Classes de l'école récupérées avec succès"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClasseResponse>> updateClasse(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClasseRequest request) {
        ClasseResponse response = classeService.updateClasse(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Classe mise à jour avec succès"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClasse(@PathVariable UUID id) {
        classeService.deleteClasse(id);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Classe supprimée avec succès"));
    }

    @PostMapping("/{id}/ues")
    public ResponseEntity<ApiResponse<ClasseResponse>> associateUEs(
            @PathVariable UUID id,
            @RequestBody List<UUID> ueIds) {
        ClasseResponse response = classeService.associateUEs(id, ueIds);
        return ResponseEntity.ok(
                ApiResponse.success(response, "UEs associées avec succès"));
    }
}
