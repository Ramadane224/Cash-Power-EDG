package com.odc.cashpoweredg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.odc.cashpoweredg.model.CreditState
import com.odc.cashpoweredg.ui.utils.Formatters
import com.odc.cashpoweredg.viewmodel.DashboardUiState
import com.odc.cashpoweredg.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToForm: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToForm,
                icon = { Icon(Icons.Default.Add, contentDescription = "Saisir") },
                text = { Text("Nouveau Relevé / Achat") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // En-tête du compteur
                HeaderCompteur(nomCompteur = uiState.nomCompteur)

                // Alerte si le crédit est bas
                if (uiState.enAlerte) {
                    AlerteCreditCard(
                        etatCredit = uiState.etatCredit,
                        onRechargerClick = onNavigateToForm
                    )
                }

                // Carte Principale - Solde & Autonomie
                CardSoldePrincipal(
                    uiState = uiState,
                    onNavigateToForm = onNavigateToForm
                )

                // Indication si aucun relevé n'a encore été fait
                if (uiState.aucunReleve) {
                    CardAvisPremierReleve(
                        onSaisirReleveClick = onNavigateToForm
                    )
                }

                // Grille de Statistiques
                Text(
                    text = "Aperçu de la consommation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Speed,
                        title = "Moyenne / jour",
                        value = uiState.consommationMoyenne?.let { Formatters.formatKwh(it) } ?: "Inconnue",
                        subtitle = "Basé sur vos relevés",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Payments,
                        title = "Coût ce mois",
                        value = Formatters.formatGnf(uiState.coutMoisEnCoursGnf),
                        subtitle = "Achats en cours",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.NotificationsActive,
                        title = "Seuil d'alerte",
                        value = Formatters.formatJours(uiState.seuilAlerte),
                        subtitle = "Seuil paramétré",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun HeaderCompteur(nomCompteur: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = nomCompteur.ifBlank { "Compteur EDG" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Suivi Cash Power EDG",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CardSoldePrincipal(
    uiState: DashboardUiState,
    onNavigateToForm: () -> Unit
) {
    val (statusColor, statusText) = when (uiState.etatCredit) {
        CreditState.NORMAL -> Color(0xFF2E7D32) to "Normal"
        CreditState.WARNING -> Color(0xFFE65100) to "Attention"
        CreditState.CRITICAL -> Color(0xFFC62828) to "Critique"
        CreditState.UNKNOWN -> Color.Gray to "Inconnu"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Crédit Restant Estimé",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                // Badge d'état
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quantité kWh
            Text(
                text = if (uiState.aucunReleve) "-- kWh" else Formatters.formatKwh(uiState.kwhRestants),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Estimation jours
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        uiState.aucunReleve -> "Enregistrez un premier relevé pour estimer l'autonomie"
                        uiState.joursRestants != null -> "Autonomie estimée : ~ ${Formatters.formatJours(uiState.joursRestants)}"
                        else -> "Autonomie : données insuffisantes"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun AlerteCreditCard(
    etatCredit: CreditState,
    onRechargerClick: () -> Unit
) {
    val isCritical = etatCredit == CreditState.CRITICAL
    val containerColor = if (isCritical) Color(0xFFFFEBEE) else Color(0xFFFFF3E0)
    val contentColor = if (isCritical) Color(0xFFC62828) else Color(0xFFE65100)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCritical) "Alerte Énergie Épuisée !" else "Alerte Solde Bas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = if (isCritical)
                        "Votre solde Cash Power est très bas ou épuisé. Rechargez rapidement."
                    else
                        "Votre réserve est sous le seuil d'alerte. Pensez à acheter du crédit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onRechargerClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = contentColor,
                    contentColor = Color.White
                )
            ) {
                Text("Acheter", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun CardAvisPremierReleve(onSaisirReleveClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bienvenue dans Cash Power EDG",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pour suivre votre consommation et obtenir des estimations précises en jours, commencez par enregistrer le nombre de kWh restant actuellement sur votre compteur.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onSaisirReleveClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Saisir un premier relevé")
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }
    }
}
