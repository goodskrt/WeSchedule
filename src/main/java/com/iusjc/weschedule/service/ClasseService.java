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

        Classe classe = new Classe();
        classe.setNom(request.getNom());
        classe.setNiveau(request.getNiveau());
        classe.setEffectif(request.getEffectif());
        classe.setLangue(request.getLangue());
        classe.setDescription(request.getDescription());

        if (request.getEcoleId() != null) {
            ecoleRepository.findById(request.getEcoleId()).ifPresent(classe::setEcole);
        }
        if (request.getFiliereId() != null) {
            filiereRepository.findById(request.getFiliereId()).ifPresent(classe::setFiliere);
        }
        if (request.getUeIds() != null && !request.getUeIds().isEmpty()) {
            Set<UE> ues = new HashSet<>(ueRepository.findAllById(request.getUeIds()));
            classe.setUes(ues);
        }

        Classe saved = classeRepository.save(classe);
        log.info("Classe created with ID: {}", saved.getIdClasse());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ClasseResponse> getAllClasses() {
        return classeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClasseResponse getClasseById(UUID id) {
        Classe classe = classeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        return mapToResponse(classe);
    }

    @Transactional(readOnly = true)
    public List<ClasseResponse> getClassesByEcole(UUID ecoleId) {
        return classeRepository.findAll().stream()
                .filter(c -> c.getEcole() != null && c.getEcole().getIdEcole().equals(ecoleId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClasseResponse updateClasse(UUID id, UpdateClasseRequest request) {
        log.info("Updating classe with ID: {}", id);

        Classe classe = classeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));

        if (request.getNom() != null)        classe.setNom(request.getNom());
        if (request.getNiveau() != null)     classe.setNiveau(request.getNiveau());
        if (request.getEffectif() != null)   classe.setEffectif(request.getEffectif());
        if (request.getLangue() != null)     classe.setLangue(request.getLangue());
        if (request.getDescription() != null) classe.setDescription(request.getDescription());

        if (request.getEcoleId() != null) {
            ecoleRepository.findById(request.getEcoleId()).ifPresent(classe::setEcole);
        }
        if (request.getFiliereId() != null) {
            filiereRepository.findById(request.getFiliereId()).ifPresent(classe::setFiliere);
        }
        if (request.getUeIds() != null) {
            Set<UE> ues = new HashSet<>(ueRepository.findAllById(request.getUeIds()));
            classe.setUes(ues);
        }

        Classe updated = classeRepository.save(classe);
        log.info("Classe updated successfully");
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteClasse(UUID id) {
        Classe classe = classeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        classeRepository.delete(classe);
        log.info("Classe deleted: {}", id);
    }

    @Transactional
    public ClasseResponse associateUEs(UUID classeId, List<UUID> ueIds) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        Set<UE> ues = new HashSet<>(ueRepository.findAllById(ueIds));
        classe.setUes(ues);
        return mapToResponse(classeRepository.save(classe));
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
                .ecoleId(classe.getEcole() != null ? classe.getEcole().getIdEcole() : null)
                .ecoleNom(classe.getEcole() != null ? classe.getEcole().getNomEcole() : null)
                .filiereId(classe.getFiliere() != null ? classe.getFiliere().getIdFiliere() : null)
                .filiereNom(classe.getFiliere() != null ? classe.getFiliere().getNomFiliere() : null)
                .effectif(classe.getEffectif())
                .langue(classe.getLangue())
                .description(classe.getDescription())
                .ues(ueResponses)
                .build();
    }
}
