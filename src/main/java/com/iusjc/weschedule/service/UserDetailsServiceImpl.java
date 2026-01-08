package com.iusjc.weschedule.service;

import com.iusjc.weschedule.models.Utilisateur;
import com.iusjc.weschedule.repositories.UtilisateurRepository;
import com.iusjc.weschedule.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Chargement de l'utilisateur : {}", email);

        return utilisateurRepository.findByEmail(email)
                .map(utilisateur -> {
                    log.debug("Utilisateur trouvé : {} ({})", email, utilisateur.getRole());
                    return new UserPrincipal(utilisateur);
                })
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouvé : {}", email);
                    return new UsernameNotFoundException("Utilisateur non trouvé: " + email);
                });
    }
}