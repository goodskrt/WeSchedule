package com.iusjc.weschedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusjc.weschedule.dto.AuthResponse;
import com.iusjc.weschedule.dto.LoginRequest;
import com.iusjc.weschedule.enums.Role;
import com.iusjc.weschedule.models.Administrateur;
import com.iusjc.weschedule.security.JwtService;
import com.iusjc.weschedule.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_ShouldReturnOk_WhenCredentialsAreValid() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setMotDePasse("password");

        AuthResponse authResponse = AuthResponse.builder()
                .success(true)
                .message("Success")
                .role(Role.ADMINISTRATEUR)
                .build();

        Administrateur user = new Administrateur();
        user.setIdUser(UUID.randomUUID());
        user.setEmail("test@test.com");
        user.setNom("Admin");
        user.setPrenom("User");
        user.setRole(Role.ADMINISTRATEUR);

        when(authService.authenticate(anyString(), anyString())).thenReturn(authResponse);
        when(authService.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mock-token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").value("mock-token"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void login_ShouldReturnBadRequest_WhenCredentialsAreInvalid() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("wrong@test.com");
        request.setMotDePasse("wrong");

        AuthResponse authResponse = AuthResponse.builder()
                .success(false)
                .message("Invalid credentials")
                .build();

        when(authService.authenticate(anyString(), anyString())).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
}
