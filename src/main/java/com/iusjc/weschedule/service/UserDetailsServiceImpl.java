package com.iusjc.weschedule.service;

import com.iusjc.weschedule.models.Utilisateur;
import com.iusjc.weschedule.repositories.UtilisateurRepository;
import com.iusjc.weschedule.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // ON AFFICHE TOUT CE QU'IL Y A DANS LA BASE (debug ultime)
        List<Utilisateur> tous = utilisateurRepository.findAll();
        System.out.println("=== CONTENU DE LA TABLE UTILISATEURS ===");
        for (Utilisateur u : tous) {
            System.out.println("ID: " + u.getIdUser() +
                    " | Email: " + u.getEmail() +
                    " | Mot de passe: " + u.getMotDePasse() +
                    " | Role: " + u.getRole());
        }
        System.out.println("=========================================");

        return utilisateurRepository.findByEmail(email)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + email));
    }
}