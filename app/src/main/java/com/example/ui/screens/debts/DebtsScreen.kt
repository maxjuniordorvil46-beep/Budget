package com.example.ui.screens.debts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Account
import com.example.data.model.Debt
import com.example.domain.DebtStrategyItem
import com.example.domain.FinancialCalculations
import com.example.ui.components.AddDebtDialog
import com.example.ui.viewmodel.BudgetViewModel

@Composable
fun DebtsScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val debts by viewModel.debts.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    var selectedStrategyTab by remember { mutableStateOf(0) } // 0: Avalanche, 1: Boule de neige
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var debtForPayment by remember { mutableStateOf<Debt?>(null) }

    val totalDebtRemaining = debts.sumOf { it.remainingBalance }
    val totalMinPayments = debts.sumOf { it.minimumPayment }

    val strategyItems: List<DebtStrategyItem> = if (selectedStrategyTab == 0) {
        FinancialCalculations.calculateAvalancheStrategy(debts)
    } else {
        FinancialCalculations.calculateSnowballStrategy(debts)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("debts_screen_list"),
            contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_debt_summary"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dette familiale restante",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFFEBEE)
                            ) {
                                Text(
                                    text = "${debts.count { it.remainingBalance > 0 }} active(s)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FinancialCalculations.formatCurrency(totalDebtRemaining, currencySymbol),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Total mensualités minimales obligatoires : ${FinancialCalculations.formatCurrency(totalMinPayments, currencySymbol)} / mois",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Strategy Selector Tabs: Avalanche vs Snowball
            item {
                Column {
                    Text(
                        text = "Stratégie de remboursement recommandée",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TabRow(
                        selectedTabIndex = selectedStrategyTab,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Tab(
                            selected = selectedStrategyTab == 0,
                            onClick = { selectedStrategyTab = 0 },
                            text = { Text("Avalanche (Taux élevé)") }
                        )
                        Tab(
                            selected = selectedStrategyTab == 1,
                            onClick = { selectedStrategyTab = 1 },
                            text = { Text("Boule de neige (Solde faible)") }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedStrategyTab == 0)
                                    "Méthode Avalanche : Vous remboursez d'abord la dette au taux le plus élevé pour minimiser les intérêts totaux payés."
                                else
                                    "Méthode Boule de neige : Vous éliminez en premier les plus petits soldes pour remporter des victoires psychologiques rapides.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Ranked Debt Cards
            if (strategyItems.isEmpty()) {
                item {
                    Text(
                        text = "Félicitations ! Aucune dette active enregistrée.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2E7D32)
                    )
                }
            } else {
                items(strategyItems, key = { it.debt.id }) { item ->
                    DebtStrategyCard(
                        item = item,
                        currencySymbol = currencySymbol,
                        onRecordPayment = { debtForPayment = item.debt },
                        onDelete = { viewModel.deleteDebt(item.debt) }
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showAddDebtDialog = true },
            icon = { Icon(Icons.Default.Add, contentDescription = "Ajouter dette") },
            text = { Text("Dette") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_debt")
        )
    }

    if (showAddDebtDialog) {
        AddDebtDialog(
            onDismiss = { showAddDebtDialog = false },
            onConfirm = { newDebt ->
                viewModel.addDebt(newDebt)
                showAddDebtDialog = false
            }
        )
    }

    // Payment Dialog (splits principal and interest)
    debtForPayment?.let { debt ->
        var totalPaymentText by remember { mutableStateOf(debt.minimumPayment.toString()) }
        var interestText by remember { mutableStateOf("0.0") }
        var selectedAccId by remember { mutableStateOf(accounts.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { debtForPayment = null },
            title = { Text("Remboursement de dette", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Créancier : ${debt.creditor}")
                    Text(
                        text = "Solde restant : ${FinancialCalculations.formatCurrency(debt.remainingBalance, currencySymbol)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = totalPaymentText,
                        onValueChange = { totalPaymentText = it.replace(',', '.') },
                        label = { Text("Montant total payé") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = interestText,
                        onValueChange = { interestText = it.replace(',', '.') },
                        label = { Text("Part d'intérêts ou frais (ex : 15.0)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Compte débité :", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    accounts.forEach { acc ->
                        FilterChip(
                            selected = selectedAccId == acc.id,
                            onClick = { selectedAccId = acc.id },
                            label = { Text("${acc.name} (${FinancialCalculations.formatCurrency(acc.currentBalance, currencySymbol)})") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val total = totalPaymentText.toDoubleOrNull() ?: 0.0
                        val interest = interestText.toDoubleOrNull() ?: 0.0
                        val principal = (total - interest).coerceAtLeast(0.0)
                        if (total > 0) {
                            viewModel.recordDebtPayment(
                                debtId = debt.id,
                                totalPaid = total,
                                principalPaid = principal,
                                interestPaid = interest,
                                accountId = selectedAccId,
                                notes = "Paiement dette mensuel"
                            )
                            debtForPayment = null
                        }
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { debtForPayment = null }) { Text("Annuler") }
            }
        )
    }
}

@Composable
fun DebtStrategyCard(
    item: DebtStrategyItem,
    currencySymbol: String = "$",
    onRecordPayment: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val debt = item.debt
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("debt_card_${debt.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "#${item.priorityRank}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = debt.creditor,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${debt.type.labelFr} • Taux ${debt.annualInterestRate}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = FinancialCalculations.formatCurrency(debt.remainingBalance, currencySymbol),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = item.rationale,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Paiement min : ${FinancialCalculations.formatCurrency(debt.minimumPayment, currencySymbol)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    Button(
                        onClick = onRecordPayment,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Payer", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
