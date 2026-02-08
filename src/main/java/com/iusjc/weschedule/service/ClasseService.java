package com.iusjc.weschedule.service;

import com.iusjc.weschedule.dto.ClasseResponse;
import com.iusjc.weschedule.dto.CreateClasseRequest;
import com.iusjc.weschedule.dto.UpdateClasseRequest;
import com.iusjc.weschedule.models.Classe;
import com.iusjc.weschedule.models.Ecole;
import com.iusjc.weschedule.models.Filiere;
import com.iusjc.weschedule.models.UE;
import com.iusjc.weschedule.repositories.ClasseRepository;
import com.iusjc.weschedule.repositories.EcoleRepository;
import com.iusjc.weschedule.repositories.FiliereRepository;
import com.iusjc.weschedule.repositories.UERepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClasseService {

    private final ClasseRepository classeRepository;
    private final EcoleRepository ecoleRepository;
    private final FiliereRepository filiereRepository;
    private final UERepository ueRepository;

    @Transactional
    public ClasseResponse createClasse(CreateClasseRequest request) {
        log.info("Creating new classe: {}", request.getNom());

        Ecole ecole = ecoleRepository.findById(request.getEcoleId())
                .orElseThrow(() -> new RuntimeException("École non trouvée"));

        Filiere filiere = filiereRepository.findById(request.getFiliereId())
                .orElseThrow(() -> new RuntimeException("Filière non trouvée"));

        Classe classe = new Classe();
        classe.setNom(request.getNom());
        classe.setEcole(ecole);
        classe.setFiliere(filiere);
        classe.setNiveau(request.getNiveau());
        classe.setEffectif(request.getEffectif());
        classe.setSemestre(request.getSemestre());
        classe.setEffectifMax(request.getEffectifMax());
        classe.setResponsable(request.getResponsable());
        classe.setDescription(request.getDescription());
        classe.setSpecialite(request.getSpecialite());

        // Associer les UEs si fournies
        if (request.getUeIds() != null && !request.getUeIds().isEmpty()) {
            Set<UE> ues = new HashSet<>(ueRepository.findAllById(request.getUeIds()));
            classe.setUes(ues);
        }

        Classe savedClasse = classeRepository.save(classe);
        log.info("Classe created successfully with ID: {}", savedClasse.getIdClasse());

        return mapToResponse(savedClasse);
    }

    @Transactional(readOnly = true)
    public List<ClasseResponse> getAllClasses() {
        log.info("Fetching all classes");
        return classeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClasseResponse getClasseById(UUID id) {
        log.info("Fetching classe with ID: {}", id);
        Classe classe = classeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        return mapToResponse(classe);
    }

    @Transactional(readOnly = true)
    public List<ClasseResponse> getClassesByEcole(UUID ecoleId) {
        log.info("Fetching classes for ecole ID: {}", ecoleId);
        Ecole ecole = ecoleRepository.findById(ecoleId)
                .orElseThrow(() -> new RuntimeException("École non trouvée"));
        return classeRepository.findAll().stream()
                .filter(c -> c.getEcole().getIdEcole().equals(ecoleId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClasseResponse updateClasse(UUID id, UpdateClasseRequest request) {
        log.info("Updating classe with ID: {}", id);

        Classe classe = classeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

        if (request.getNom() != null) {
            classe.setNom(request.getNom());
        }
        if (request.getEcoleId() != null) {
            Ecole ecole = ecoleRepository.findById(request.getEcoleId())
                    .orElseThrow(() -> new RuntimeException("École non trouvée"));
            classe.setEcole(ecole);
        }
        if (request.getFiliereId() != null) {
            Filiere filiere = filiereRepository.findById(request.getFiliereId())
                    .orElseThrow(() -> new RuntimeException("Filière non trouvée"));
            classe.setFiliere(filiere);
        }
        if (request.getNiveau() != null) {
            classe.setNiveau(request.getNiveau());
        }
        if (request.getEffectif() != null) {
            classe.setEffectif(request.getEffectif());
        }
        if (request.getSemestre() != null) {
            classe.setSemestre(request.getSemestre());
        }
        if (request.getEffectifMax() != null) {
            classe.setEffectifMax(request.getEffectifMax());
        }
        if (request.getResponsable() != null) {
            classe.setResponsable(request.getResponsable());
        }
        if (request.getDescription() != null) {
            classe.setDescription(request.getDescription());
        }
        if (request.getSpecialite() != null) {
            classe.setSpecialite(request.getSpecialite());
        }

        // Mettre à jour les UEs si fournies
        if (request.getUeIds() != null) {
            Set<UE> ues = new HashSet<>(ueRepository.findAllById(request.getUeIds()));
            classe.setUes(ues);
        }

        Classe updatedClasse = classeRepository.save(classe);
        log.info("Classe updated successfully");

        return mapToResponse(updatedClasse);
    }

    @Transactional
    public void deleteClasse(UUID id) {
        log.info("Deleting classe with ID: {}", id);
        Classe classe = classeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        classeRepository.delete(classe);
        log.info("Classe deleted successfully");
    }

    @Transactional
    public ClasseResponse associateUEs(UUID classeId, List<UUID> ueIds) {
        log.info("Associating UEs to classe ID: {}", classeId);

        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

        Set<UE> ues = new HashSet<>(ueRepository.findAllById(ueIds));
        classe.setUes(ues);

        Classe updatedClasse = classeRepository.save(classe);
        log.info("UEs associated successfully");

        return mapToResponse(updatedClasse);
    }

    private ClasseResponse mapToResponse(Classe classe) {
        List<ClasseResponse.UESimpleResponse> ueResponses = null;
        if (classe.getUes() != null) {
            ueResponses = classe.getUes().stream()
                    .map(ue -> ClasseResponse.UESimpleResponse.builder()
                            .id(ue.getIdUE())
                            .code(ue.getCode())
                            .nom(ue.getIntitule())
                            .build())
                    .collect(Collectors.toList());
        }

        return ClasseResponse.builder()
                .id(classe.getIdClasse())
                .nom(classe.getNom())
                .niveau(classe.getNiveau())
                .ecoleId(classe.getEcole().getIdEcole())
                .ecoleNom(classe.getEcole().getNomEcole())
                .filiereId(classe.getFiliere().getIdFiliere())
                .filiereNom(classe.getFiliere().getNomFiliere())
                .effectif(classe.getEffectif())
                .semestre(classe.getSemestre())
                .effectifMax(classe.getEffectifMax())
                .responsable(classe.getResponsable())
                .description(classe.getDescription())
                .specialite(classe.getSpecialite())
                .ues(ueResponses)
                .build();
    }
}
