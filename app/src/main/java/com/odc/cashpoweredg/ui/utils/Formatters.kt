package com.odc.cashpoweredg.ui.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilitaire de formatage pour l'interface utilisateur.
 */
object Formatters {

    private val gnfFormat = NumberFormat.getNumberInstance(Locale.FRANCE)
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
    private val dateShortFormat = SimpleDateFormat("dd MMM yyyy", Locale.FRANCE)
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.FRANCE)

    fun formatGnf(montant: Long): String {
        return "${gnfFormat.format(montant)} GNF"
    }

    fun formatKwh(kwh: Double): String {
        return String.format(Locale.FRANCE, "%.2f kWh", kwh)
    }

    fun formatJours(jours: Double): String {
        return String.format(Locale.FRANCE, "%.1f jours", jours)
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatDateCourt(timestamp: Long): String {
        return dateShortFormat.format(Date(timestamp))
    }

    fun formatMois(annee: Int, mois: Int): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.YEAR, annee)
        calendar.set(java.util.Calendar.MONTH, mois - 1)
        return monthFormat.format(calendar.time).replaceFirstChar { it.uppercase() }
    }
}
