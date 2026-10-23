package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Account
import com.example.data.model.Bill
import com.example.data.model.BillFrequency
import com.example.data.model.Category
import com.example.data.model.Debt
import com.example.data.model.HouseholdMember
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import com.example.data.model.TransactionType

@Composable
fun AddEditTransactionDialog(
    initialTransaction: Transaction? = null,
    categories: List<Category>,
    accounts: List<Account>,
    members: List<HouseholdMember>,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit
) {
    val isEditing = initialTransaction != null

    var type by remember { mutableStateOf(initialTransaction?.type ?: TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf(initialTransaction?.amount?.toString() ?: "") }
    var description by remember { mutableStateOf(initialTransaction?.description ?: "") }
    var selectedCategory by remember {
        mutableStateOf(
            initialTransaction?.categoryName ?: (categories.firstOrNull { it.type == type }?.name ?: "Courses alimentaires")
        )
    }
    var selectedAccountId by remember { mutableStateOf(initialTransaction?.accountId ?: (accounts.firstOrNull()?.id ?: 1L)) }
    var selectedTargetAccountId by remember { mutableStateOf(initialTransaction?.targetAccountId ?: (accounts.getOrNull(1)?.id ?: 2L)) }
    var selectedMember by remember { mutableStateOf(initialTransaction?.memberName ?: (members.firstOrNull()?.name ?: "Famille")) }
    var paymentMethod by remember { mutableStateOf(initialTransaction?.paymentMethod ?: "Carte") }
    var isFixed by remember { mutableStateOf(initialTransaction?.isFixed ?: false) }
    var isEssential by remember { mutableStateOf(initialTransaction?.isEssential ?: true) }
    var notes by remember { mutableStateOf(initialTransaction?.notes ?: "") }

    val filteredCategories = categories.filter { it.type == type }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Modifier la transaction" else "Ajouter une transaction",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Type selection chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionType.values().forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = {
                                type = t
                                val match = categories.firstOrNull { it.type == t }
                                if (match != null) selectedCategory = match.name
                            },
                            label = { Text(t.labelFr) },
                            modifier = Modifier.testTag("chip_type_${t.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.replace(',', '.') },
                    label = { Text("Montant") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_transaction_amount"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().testTag("input_transaction_desc"),
                    singleLine = true
                )

                if (type != TransactionType.TRANSFER) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Catégorie :", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filteredCategories.take(4).forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat.name,
                                onClick = { selectedCategory = cat.name },
                                label = { Text(cat.name, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Account
                Text(
                    text = if (type == TransactionType.TRANSFER) "Compte source :" else "Compte utilisé :",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accounts.take(3).forEach { acc ->
                        FilterChip(
                            selected = selectedAccountId == acc.id,
                            onClick = { selectedAccountId = acc.id },
                            label = { Text(acc.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                if (type == TransactionType.TRANSFER) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Compte destination :", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        accounts.filter { it.id != selectedAccountId }.take(3).forEach { acc ->
                            FilterChip(
                                selected = selectedTargetAccountId == acc.id,
                                onClick = { selectedTargetAccountId = acc.id },
                                label = { Text(acc.name, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Member
                Text("Membre de la famille :", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    members.forEach { m ->
                        FilterChip(
                            selected = selectedMember == m.name,
                            onClick = { selectedMember = m.name },
                            label = { Text(m.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                if (type == TransactionType.EXPENSE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dépense fixe (récurrente)", style = MaterialTheme.typography.bodySmall)
                        Switch(checked = isFixed, onCheckedChange = { isFixed = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dépense essentielle (besoin de base)", style = MaterialTheme.typography.bodySmall)
                        Switch(checked = isEssential, onCheckedChange = { isEssential = it })
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (facultatif)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        val tx = if (initialTransaction != null) {
                            initialTransaction.copy(
                                type = type,
                                amount = amount,
                                categoryName = if (type == TransactionType.TRANSFER) "Transfert" else selectedCategory,
                                description = description.ifBlank { selectedCategory },
                                paymentMethod = paymentMethod,
                                accountId = selectedAccountId,
                                targetAccountId = if (type == TransactionType.TRANSFER) selectedTargetAccountId else null,
                                memberName = selectedMember,
                                isFixed = isFixed,
                                isEssential = isEssential,
                                notes = notes
                            )
                        } else {
                            Transaction(
                                type = type,
                                amount = amount,
                                dateMillis = System.currentTimeMillis(),
                                categoryName = if (type == TransactionType.TRANSFER) "Transfert" else selectedCategory,
                                description = description.ifBlank { selectedCategory },
                                paymentMethod = paymentMethod,
                                accountId = selectedAccountId,
                                targetAccountId = if (type == TransactionType.TRANSFER) selectedTargetAccountId else null,
                                memberName = selectedMember,
                                isFixed = isFixed,
                                isEssential = isEssential,
                                notes = notes
                            )
                        }
                        onConfirm(tx)
                    }
                },
                modifier = Modifier.testTag("btn_confirm_add_tx")
            ) {
                Text(if (isEditing) "Enregistrer" else "Ajouter")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun AddTransactionDialog(
    categories: List<Category>,
    accounts: List<Account>,
    members: List<HouseholdMember>,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit
) {
    AddEditTransactionDialog(
        initialTransaction = null,
        categories = categories,
        accounts = accounts,
        members = members,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
fun AddEditBillDialog(
    initialBill: Bill? = null,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Bill) -> Unit
) {
    val isEditing = initialBill != null
    var name by remember { mutableStateOf(initialBill?.name ?: "") }
    var amountText by remember { mutableStateOf(initialBill?.amount?.toString() ?: "") }
    var categoryName by remember { mutableStateOf(initialBill?.categoryName ?: "Logement ou hypothèque") }
    var frequency by remember { mutableStateOf(initialBill?.frequency ?: BillFrequency.MONTHLY) }
    var daysUntilDue by remember { mutableStateOf("7") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Modifier la facture" else "Ajouter une facture",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom (ex : Électricité, Loyer)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.replace(',', '.') },
                    label = { Text("Montant prévu") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Catégorie") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (!isEditing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = daysUntilDue,
                        onValueChange = { daysUntilDue = it },
                        label = { Text("Échéance dans combien de jours ?") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val days = daysUntilDue.toLongOrNull() ?: 7L
                    if (name.isNotBlank() && amount > 0) {
                        val bill = if (initialBill != null) {
                            initialBill.copy(
                                name = name,
                                amount = amount,
                                categoryName = categoryName,
                                frequency = frequency
                            )
                        } else {
                            Bill(
                                name = name,
                                amount = amount,
                                dueDateMillis = System.currentTimeMillis() + (days * 24 * 3600 * 1000L),
                                categoryName = categoryName,
                                frequency = frequency
                            )
                        }
                        onConfirm(bill)
                    }
                }
            ) {
                Text(if (isEditing) "Enregistrer" else "Créer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun AddBillDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Bill) -> Unit
) {
    AddEditBillDialog(
        initialBill = null,
        categories = categories,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
fun AddEditSavingsGoalDialog(
    initialGoal: SavingsGoal? = null,
    onDismiss: () -> Unit,
    onConfirm: (SavingsGoal) -> Unit
) {
    val isEditing = initialGoal != null
    var name by remember { mutableStateOf(initialGoal?.name ?: "") }
    var targetText by remember { mutableStateOf(initialGoal?.targetAmount?.toString() ?: "") }
    var currentSavedText by remember { mutableStateOf(initialGoal?.currentAmount?.toString() ?: "0") }
    var targetMonths by remember { mutableStateOf("12") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Modifier l'objectif d'épargne" else "Nouvel objectif d'épargne",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du projet (ex : Achat maison)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it.replace(',', '.') },
                    label = { Text("Montant cible") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentSavedText,
                    onValueChange = { currentSavedText = it.replace(',', '.') },
                    label = { Text("Montant actuellement épargné") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (!isEditing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = targetMonths,
                        onValueChange = { targetMonths = it },
                        label = { Text("Délai visé (mois)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toDoubleOrNull() ?: 0.0
                    val current = currentSavedText.toDoubleOrNull() ?: 0.0
                    val months = targetMonths.toLongOrNull() ?: 12L
                    if (name.isNotBlank() && target > 0) {
                        val goal = if (initialGoal != null) {
                            initialGoal.copy(
                                name = name,
                                targetAmount = target,
                                currentAmount = current
                            )
                        } else {
                            SavingsGoal(
                                name = name,
                                targetAmount = target,
                                currentAmount = current,
                                targetDateMillis = System.currentTimeMillis() + (months * 30L * 24 * 3600 * 1000L)
                            )
                        }
                        onConfirm(goal)
                    }
                }
            ) {
                Text(if (isEditing) "Enregistrer" else "Créer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun AddSavingsGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (SavingsGoal) -> Unit
) {
    AddEditSavingsGoalDialog(
        initialGoal = null,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
fun EditBudgetCategoryDialog(
    categoryName: String,
    currentAllocated: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    onDelete: () -> Unit
) {
    var amountText by remember { mutableStateOf(currentAllocated.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Modifier le budget : $categoryName",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Ajustez le plafond budgétaire mensuel alloué pour cette catégorie :", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.replace(',', '.') },
                    label = { Text("Montant mensuel alloué") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer")
                }
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount >= 0) {
                            onConfirm(amount)
                        }
                    }
                ) {
                    Text("Valider")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun AddDebtDialog(
    onDismiss: () -> Unit,
    onConfirm: (Debt) -> Unit
) {
    var creditor by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf("15.0") }
    var minPaymentText by remember { mutableStateOf("50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enregistrer une dette", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = creditor,
                    onValueChange = { creditor = it },
                    label = { Text("Créancier / Nom du prêt") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it.replace(',', '.') },
                    label = { Text("Solde restant dû") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it.replace(',', '.') },
                    label = { Text("Taux d'intérêt annuel (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = minPaymentText,
                    onValueChange = { minPaymentText = it.replace(',', '.') },
                    label = { Text("Paiement minimum mensuel") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val balance = balanceText.toDoubleOrNull() ?: 0.0
                    val rate = rateText.toDoubleOrNull() ?: 0.0
                    val minPay = minPaymentText.toDoubleOrNull() ?: 0.0
                    if (creditor.isNotBlank() && balance > 0) {
                        val debt = Debt(
                            creditor = creditor,
                            totalOriginalAmount = balance,
                            remainingBalance = balance,
                            annualInterestRate = rate,
                            minimumPayment = minPay
                        )
                        onConfirm(debt)
                    }
                }
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
