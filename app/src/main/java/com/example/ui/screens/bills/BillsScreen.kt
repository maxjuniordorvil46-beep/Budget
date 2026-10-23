package com.example.ui.screens.bills

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Account
import com.example.data.model.Bill
import com.example.data.model.BillStatus
import com.example.domain.FinancialCalculations
import com.example.ui.components.AddBillDialog
import com.example.ui.components.AddEditBillDialog
import com.example.ui.viewmodel.BudgetViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BillsScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val bills by viewModel.bills.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0: A payer, 1: Payees, 2: Toutes
    var showAddDialog by remember { mutableStateOf(false) }
    var billToPay by remember { mutableStateOf<Bill?>(null) }
    var billToEdit by remember { mutableStateOf<Bill?>(null) }

    val pendingBills = bills.filter { it.status != BillStatus.PAID }
    val paidBills = bills.filter { it.status == BillStatus.PAID }
    val displayedBills = when (selectedTabIndex) {
        0 -> pendingBills
        1 -> paidBills
        else -> bills
    }

    val totalPending = pendingBills.sumOf { it.amount }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            // Header summary
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Factures restant à régler",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FinancialCalculations.formatCurrency(totalPending, currencySymbol),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (totalPending > 0) Color(0xFFC62828) else Color(0xFF2E7D32)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${pendingBills.size} en attente",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("À payer (${pendingBills.size})") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Payées (${paidBills.size})") }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Toutes (${bills.size})") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("bills_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                if (displayedBills.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aucune facture dans cette section.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(displayedBills, key = { it.id }) { bill ->
                        BillItemCard(
                            bill = bill,
                            currencySymbol = currencySymbol,
                            onPayClicked = { billToPay = bill },
                            onEdit = { billToEdit = bill },
                            onDelete = { viewModel.deleteBill(bill) }
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            icon = { Icon(Icons.Default.Add, contentDescription = "Nouvelle facture") },
            text = { Text("Facture") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_bill")
        )
    }

    billToEdit?.let { bill ->
        AddEditBillDialog(
            initialBill = bill,
            categories = categories,
            onDismiss = { billToEdit = null },
            onConfirm = { updatedBill ->
                viewModel.updateBill(updatedBill)
                billToEdit = null
            }
        )
    }

    if (showAddDialog) {
        AddBillDialog(
            categories = categories,
            onDismiss = { showAddDialog = false },
            onConfirm = { newBill ->
                viewModel.addBill(newBill)
                showAddDialog = false
            }
        )
    }

    // Dialog to pick account when paying a bill
    billToPay?.let { bill ->
        var selectedAccId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1L) }
        AlertDialog(
            onDismissRequest = { billToPay = null },
            title = { Text("Confirmer le paiement", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Régler « ${bill.name} » pour ${FinancialCalculations.formatCurrency(bill.amount, currencySymbol)} ?")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Sélectionner le compte de prélèvement :", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
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
                        viewModel.markBillPaid(bill, selectedAccId)
                        billToPay = null
                    }
                ) {
                    Text("Marquer comme payée")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { billToPay = null }) { Text("Annuler") }
            }
        )
    }
}

@Composable
fun BillItemCard(
    bill: Bill,
    currencySymbol: String = "$",
    onPayClicked: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE)
    val dueDateStr = sdf.format(Date(bill.dueDateMillis))
    val isPaid = bill.status == BillStatus.PAID
    val isOverdue = !isPaid && bill.dueDateMillis < System.currentTimeMillis()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bill_item_${bill.id}")
            .then(if (onEdit != null) Modifier.clickable { onEdit() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isPaid) Color(0xFFE8F5E9) else if (isOverdue) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPaid) Icons.Default.CheckCircle else if (isOverdue) Icons.Default.Error else Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = if (isPaid) Color(0xFF2E7D32) else if (isOverdue) Color(0xFFC62828) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = bill.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${bill.categoryName} • ${bill.frequency.labelFr}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = FinancialCalculations.formatCurrency(bill.amount, currencySymbol),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPaid) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPaid) "Payée le $dueDateStr" else "Échéance : $dueDateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverdue) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                )

                Row {
                    if (!isPaid) {
                        Button(
                            onClick = onPayClicked,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Payer", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier facture",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer facture",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
