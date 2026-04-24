package com.iusjc.weschedule.controller;

import com.iusjc.weschedule.enums.StatutSalle;
import com.iusjc.weschedule.enums.TypeSalle;
import com.iusjc.weschedule.models.Salle;
import com.iusjc.weschedule.repositories.EquipmentRepository;
import com.iusjc.weschedule.repositories.SalleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SalleController.class)
@AutoConfigureMockMvc(addFilters = false)
class SalleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SalleRepository salleRepository;

    @MockitoBean
    private EquipmentRepository equipmentRepository;

    @MockitoBean
    private com.iusjc.weschedule.security.JwtService jwtService;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    void listeSalles_ShouldReturnViewWithAttributes() throws Exception {
        // Arrange
        when(salleRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/admin/salles"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/salles-liste"))
                .andExpect(model().attributeExists("salles", "typesSalle", "statuts", "pageStats"));
    }

    @Test
    void creerSalle_ShouldSucceed_AndRedirect() throws Exception {
        // Arrange
        when(salleRepository.findByNomSalle("Salle 101")).thenReturn(Optional.empty());
        when(salleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act & Assert
        mockMvc.perform(post("/admin/salles/creer")
                .param("nomSalle", "Salle 101")
                .param("typeSalle", "SALLE_DE_COURS")
                .param("capacite", "30")
                .param("statut", "DISPONIBLE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/salles"))
                .andExpect(flash().attribute("success", "Salle créée avec succès"));
    }

    @Test
    void creerSalle_ShouldFail_WhenNameExists() throws Exception {
        // Arrange
        Salle existing = new Salle();
        existing.setNomSalle("Salle 101");
        existing.setIdSalle(UUID.randomUUID());
        when(salleRepository.findByNomSalle("Salle 101")).thenReturn(Optional.of(existing));

        // Act & Assert
        mockMvc.perform(post("/admin/salles/creer")
                .param("nomSalle", "Salle 101")
                .param("typeSalle", "SALLE_DE_COURS")
                .param("capacite", "30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/salles/nouvelle"))
                .andExpect(flash().attribute("error", "Une salle avec ce nom existe déjà"));
    }

    @Test
    void detailsSalle_ShouldReturnView() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        Salle salle = new Salle();
        salle.setIdSalle(id);
        salle.setTypeSalle(TypeSalle.SALLE_DE_COURS);
        when(salleRepository.findById(id)).thenReturn(Optional.of(salle));

        // Act & Assert
        mockMvc.perform(get("/admin/salles/details/" + id))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/salle-details"))
                .andExpect(model().attribute("salle", salle));
    }
}
