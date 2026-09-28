package com.odc.cashpoweredg.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ValidationTest {

    private fun invalide(message: String) = ValidationResult.Invalid(message)

    @Test
    fun `montant valide avec ou sans separateur de milliers`() {
        assertEquals(ValidationResult.Valid(100_000L), validateMontant("100000"))
        assertEquals(ValidationResult.Valid(100_000L), validateMontant(" 100 000 "))
        assertEquals(ValidationResult.Valid(100_000L), validateMontant("100 000"))
    }

    @Test
    fun `montant vide, nul, negatif ou non numerique`() {
        assertEquals(invalide(MessagesErreur.CHAMPS_VIDES), validateMontant(""))
        assertEquals(invalide(MessagesErreur.CHAMPS_VIDES), validateMontant("   "))
        assertEquals(invalide(MessagesErreur.MONTANT_INVALIDE), validateMontant("0"))
        assertEquals(invalide(MessagesErreur.MONTANT_INVALIDE), validateMontant("-5000"))
        assertEquals(invalide(MessagesErreur.NOMBRE_INVALIDE), validateMontant("abc"))
        assertEquals(invalide(MessagesErreur.NOMBRE_INVALIDE), validateMontant("12,5"))
    }

    @Test
    fun `kWh avec virgule ou point decimal`() {
        assertEquals(ValidationResult.Valid(42.5), validateKwh("42,5"))
        assertEquals(ValidationResult.Valid(42.5), validateKwh("42.5"))
        assertEquals(ValidationResult.Valid(0.0), validateKwh("0"))
    }

    @Test
    fun `kWh vide, negatif ou non numerique`() {
        assertEquals(invalide(MessagesErreur.CHAMPS_VIDES), validateKwh(""))
        assertEquals(invalide(MessagesErreur.KWH_NEGATIF), validateKwh("-1"))
        assertEquals(invalide(MessagesErreur.NOMBRE_INVALIDE), validateKwh("douze"))
        assertEquals(invalide(MessagesErreur.NOMBRE_INVALIDE), validateKwh("NaN"))
    }

    @Test
    fun `tarif`() {
        assertEquals(ValidationResult.Valid(2_000.0), validateTarif("2 000"))
        assertEquals(invalide(MessagesErreur.TARIF_INVALIDE), validateTarif("0"))
        assertEquals(invalide(MessagesErreur.TARIF_INVALIDE), validateTarif("-2000"))
        assertEquals(invalide(MessagesErreur.CHAMPS_VIDES), validateTarif(""))
    }

    @Test
    fun `seuil d alerte`() {
        assertEquals(ValidationResult.Valid(2.0), validateSeuil("2"))
        assertEquals(ValidationResult.Valid(1.5), validateSeuil("1,5"))
        assertEquals(invalide(MessagesErreur.SEUIL_INVALIDE), validateSeuil("-1"))
        assertEquals(invalide(MessagesErreur.CHAMPS_VIDES), validateSeuil(""))
    }

    @Test
    fun `nom du compteur`() {
        assertEquals(ValidationResult.Valid("Compteur Maison"), validateNomCompteur("  Compteur Maison "))
        assertEquals(invalide(MessagesErreur.CHAMPS_VIDES), validateNomCompteur("   "))
    }

    @Test
    fun `date`() {
        val maintenant = 1_000_000L
        assertEquals(ValidationResult.Valid(maintenant), validateDate(maintenant, maintenant))
        assertEquals(invalide(MessagesErreur.CHAMPS_VIDES), validateDate(null, maintenant))
        assertEquals(invalide(MessagesErreur.DATE_INVALIDE), validateDate(0, maintenant))
        assertEquals(invalide(MessagesErreur.DATE_FUTURE), validateDate(maintenant + 1, maintenant))
    }

    @Test
    fun `format pour les champs de saisie`() {
        assertEquals("2000", formatPourSaisie(2_000.0))
        assertEquals("2,5", formatPourSaisie(2.5))
    }
}
