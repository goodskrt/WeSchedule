package com.iusjc.weschedule.util;

import com.iusjc.weschedule.enums.StatutEquipement;
import com.iusjc.weschedule.models.Equipment;

/**
 * Règle métier : un équipement affecté à une salle est automatiquement « en service ».
 * Sans salle, le statut choisi (ou DISPONIBLE par défaut) s'applique.
 */
public final class EquipmentStatutRules {

    private EquipmentStatutRules() {}

    public static void syncStatutAvecSalle(Equipment eq, StatutEquipement statutSiSansSalle) {
        if (eq.getSalle() != null) {
            eq.setStatut(StatutEquipement.EN_SERVICE);
        } else {
            StatutEquipement s = statutSiSansSalle != null ? statutSiSansSalle : StatutEquipement.DISPONIBLE;
            eq.setStatut(s);
        }
    }
}
