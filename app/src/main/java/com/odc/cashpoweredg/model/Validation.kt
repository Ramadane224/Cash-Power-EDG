package com.odc.cashpoweredg.model

/** R6 — Messages d'erreur affichés à l'utilisateur. */
object MessagesErreur {
    const val MONTANT_INVALIDE = "Le montant doit être supérieur à 0."
    const val TARIF_INVALIDE = "Le tarif doit être supérieur à 0."
    const val KWH_NEGATIF = "Le nombre de kWh doit être positif."
    const val CHAMPS_VIDES = "Veuillez remplir tous les champs."
    const val NOMBRE_INVALIDE = "Veuillez saisir un nombre valide."
    const val SEUIL_INVALIDE = "Le seuil d'alerte doit être positif."
    const val DATE_INVALIDE = "La date est invalide."
    const val DATE_FUTURE = "La date ne peut pas être dans le futur."
    const val TARIF_NON_CONFIGURE = "Configurez d'abord le tarif du kWh dans les paramètres."
    const val CHARGEMENT = "Impossible de charger les données."
    const val ENREGISTREMENT = "L'enregistrement a échoué. Veuillez réessayer."
}

/** Résultat d'une validation : la valeur convertie, ou le message d'erreur à afficher. */
sealed interface ValidationResult<out T> {
    data class Valid<T>(val value: T) : ValidationResult<T>
    data class Invalid(val message: String) : ValidationResult<Nothing>
}

private val ENTIER = Regex("""-?\d+""")
private val DECIMAL = Regex("""-?\d+(\.\d+)?""")

/** Retire les espaces (y compris insécables) utilisés comme séparateurs de milliers. */
private fun String.sansEspaces(): String =
    trim().replace(" ", "").replace(" ", "").replace(" ", "")

/** "100 000" → 100000, ou `null` si la saisie n'est pas un entier. */
private fun parseEntier(saisie: String): Long? =
    saisie.sansEspaces().takeIf { ENTIER.matches(it) }?.toLongOrNull()

/** "42,5" ou "42.5" → 42.5, ou `null` si la saisie n'est pas un nombre. */
private fun parseDecimal(saisie: String): Double? =
    saisie.sansEspaces().replace(',', '.')
        .takeIf { DECIMAL.matches(it) }
        ?.toDoubleOrNull()
        ?.takeIf { it.isFinite() }

/** Montant d'un achat en GNF : entier strictement positif. */
fun validateMontant(saisie: String): ValidationResult<Long> {
    if (saisie.isBlank()) return ValidationResult.Invalid(MessagesErreur.CHAMPS_VIDES)
    val montant = parseEntier(saisie) ?: return ValidationResult.Invalid(MessagesErreur.NOMBRE_INVALIDE)
    if (montant <= 0) return ValidationResult.Invalid(MessagesErreur.MONTANT_INVALIDE)
    return ValidationResult.Valid(montant)
}

/** kWh restants d'un relevé : nombre positif ou nul (0 = crédit épuisé). */
fun validateKwh(saisie: String): ValidationResult<Double> {
    if (saisie.isBlank()) return ValidationResult.Invalid(MessagesErreur.CHAMPS_VIDES)
    val kwh = parseDecimal(saisie) ?: return ValidationResult.Invalid(MessagesErreur.NOMBRE_INVALIDE)
    if (kwh < 0) return ValidationResult.Invalid(MessagesErreur.KWH_NEGATIF)
    return ValidationResult.Valid(kwh)
}

/** Tarif du kWh en GNF : strictement positif. */
fun validateTarif(saisie: String): ValidationResult<Double> {
    if (saisie.isBlank()) return ValidationResult.Invalid(MessagesErreur.CHAMPS_VIDES)
    val tarif = parseDecimal(saisie) ?: return ValidationResult.Invalid(MessagesErreur.NOMBRE_INVALIDE)
    if (tarif <= 0) return ValidationResult.Invalid(MessagesErreur.TARIF_INVALIDE)
    return ValidationResult.Valid(tarif)
}

/** Seuil d'alerte en jours : positif ou nul. */
fun validateSeuil(saisie: String): ValidationResult<Double> {
    if (saisie.isBlank()) return ValidationResult.Invalid(MessagesErreur.CHAMPS_VIDES)
    val seuil = parseDecimal(saisie) ?: return ValidationResult.Invalid(MessagesErreur.NOMBRE_INVALIDE)
    if (seuil < 0) return ValidationResult.Invalid(MessagesErreur.SEUIL_INVALIDE)
    return ValidationResult.Valid(seuil)
}

/** Nom du compteur : obligatoire, espaces superflus retirés. */
fun validateNomCompteur(saisie: String): ValidationResult<String> {
    if (saisie.isBlank()) return ValidationResult.Invalid(MessagesErreur.CHAMPS_VIDES)
    return ValidationResult.Valid(saisie.trim())
}

/** Date d'un achat ou d'un relevé (millisecondes) : obligatoire et pas dans le futur. */
fun validateDate(date: Long?, maintenant: Long): ValidationResult<Long> {
    if (date == null) return ValidationResult.Invalid(MessagesErreur.CHAMPS_VIDES)
    if (date <= 0) return ValidationResult.Invalid(MessagesErreur.DATE_INVALIDE)
    if (date > maintenant) return ValidationResult.Invalid(MessagesErreur.DATE_FUTURE)
    return ValidationResult.Valid(date)
}

/** Prépare une valeur pour un champ de saisie : 2000.0 → "2000", 2.5 → "2,5". */
fun formatPourSaisie(valeur: Double): String =
    if (valeur % 1.0 == 0.0) valeur.toLong().toString() else valeur.toString().replace('.', ',')
