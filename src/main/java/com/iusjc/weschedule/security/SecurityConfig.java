package com.iusjc.weschedule.security;

import com.iusjc.weschedule.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Désactiver CSRF
                .csrf(csrf -> csrf.disable())

                // 2. Gestion des sessions
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // 3. Règles d'accès
                .authorizeHttpRequests(auth -> auth
                        // URLs publiques
                        .requestMatchers("/", "/login", "/register", "/admin/signup", 
                                "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        
                        // API REST d'authentification (publique)
                        .requestMatchers("/api/auth/**").permitAll()
                        
                        // Pages et API endpoints pour mot de passe oublié/réinitialisation
                        .requestMatchers("/reset-password-request", "/reset-password", "/reset-password-error", 
                                "/reset-password-success", "/api/forgot-password", "/api/reset-password-with-token", 
                                "/api/auto-login", "/api/validate-new-password").permitAll()

                        // Toutes les autres pages → authentication requise
                        .anyRequest().authenticated()
                )

                // 4. Configuration du formulaire de login
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", false)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // 5. Logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )

                // 6. JWT Filter (optionnel si on utilise des sessions)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
