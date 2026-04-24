package com.iusjc.weschedule.dto;

/**
 * Pastille de statistiques affichée en haut des pages d'administration (fragment admin-stats).
 */
public record AdminStatChip(String label, String value, String icon, String hint) {

    public AdminStatChip(String label, String value, String icon) {
        this(label, value, icon, null);
    }
}
