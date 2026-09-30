package com.odc.cashpoweredg.model

/**
 * R5 — État du crédit affiché sur le tableau de bord.
 *
 * Calculé par [determineCreditState] ; l'interface se contente de l'afficher.
 */
enum class CreditState {
    /** Estimation impossible : pas assez de relevés exploitables. */
    UNKNOWN,

    /** Jours restants > seuil d'alerte. */
    NORMAL,

    /** 0 < jours restants <= seuil d'alerte. */
    WARNING,

    /** Crédit épuisé : jours restants <= 0. */
    CRITICAL
}
