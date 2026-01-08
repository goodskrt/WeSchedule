package com.iusjc.weschedule.security;

import com.iusjc.weschedule.models.Utilisateur;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Utilisateur utilisateur;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name()));
    }

    @Override
    public String getPassword() {
        return utilisateur.getMotDePasse();
    }

    @Override
    public String getUsername() {
        return utilisateur.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    public String getNomComplet() {
        return utilisateur.getPrenom() + " " + utilisateur.getNom();
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }
    
    @Override
    public String toString() {
        return "UserPrincipal{" +
                "username='" + getUsername() + '\'' +
                ", role=" + utilisateur.getRole() +
                ", nomComplet='" + getNomComplet() + '\'' +
                '}';
    }
}