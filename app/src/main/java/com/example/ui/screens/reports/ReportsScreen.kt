package com.example.ui.screens.reports

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionType
import com.example.domain.FinancialCalculations
import com.example.ui.components.ChartSlice
import com.example.ui.components.ComparisonBarChart
import com.example.ui.components.DonutExpenseChart
import com.example.ui.viewmodel.BudgetViewModel

@Composable
fun ReportsScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    var selectedPeriod by remember { mutableStateOf("Ce mois") }

    val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
    val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = expenses.sumOf { it.amount }

    // Breakdown by category
    val categoryColors = listOf(
        Color(0xFFE53935), Color(0xFFFB8C00), Color(0xFF00ACC1),
        Color(0xFF43A047), Color(0xFF5E35B1), Color(0xFF3949AB),
        Color(0xFFD81B60), Color(0xFF8E24AA), Color(0xFF00897B),
        Color(0xFF1E88E5), Color(0xFF6D4C41), Color(0xFF546E7A)
    )

    val expenseByCategory = expenses
        .groupBy { it.categoryName }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .entries
        .sortedByDescending { it.value }

    val donutSlices = expenseByCategory.mapIndexed { index, entry ->
        ChartSlice(
            label = entry.key,
            value = entry.value,
            color = categoryColors[index % categoryColors.size]
        )
    }

    // Fixed vs Variable
    val fixedExpenses = expenses.filter { it.isFixed }.sumOf { it.amount }
    val variableExpenses = expenses.filter { !it.isFixed }.sumOf { it.amount }

    // Essential vs Optional
    val essentialExpenses = expenses.filter { it.isEssential }.sumOf { it.amount }
    val optionalExpenses = expenses.filter { !it.isEssential }.sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("reports_screen_list"),
        contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period filter chips & Export
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Ce mois", "Toutes").forEach { p ->
                        FilterChip(
                            selected = selectedPeriod == p,
                            onClick = { selectedPeriod = p },
                            label = { Text(p, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Button(
                    onClick = {
                        val csvData = viewModel.exportTransactionsCsv()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, csvData)
                            putExtra(Intent.EXTRA_TITLE, "Export Finances Familiales CSV")
                            type = "text/csv"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Exporter les transactions")
                        context.startActivity(shareIntent)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_export_csv")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Export CSV", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Donut Chart: Expenses by Category
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Répartition des dépenses par catégorie",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    DonutExpenseChart(
                        slices = donutSlices,
                        currencySymbol = currencySymbol
                    )
                }
            }
        }

        // Comparison Bar Chart: Income vs Expense
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Comparaison Revenus vs Dépenses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ComparisonBarChart(
                        income = income,
                        expense = totalExpense,
                        currencySymbol = currencySymbol
                    )
                }
            }
        }

        // Fixed vs Variable Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dépenses Fixes vs Variables",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Dépenses Fixes (loyer, abonnements)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = FinancialCalculations.formatCurrency(fixedExpenses, currencySymbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Dépenses Variables (courses, sorties)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = FinancialCalculations.formatCurrency(variableExpenses, currencySymbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF6C00)
                            )
                        }
                    }
                }
            }
        }

        // Essential vs Optional Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Besoins Essentiels vs Facultatifs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Essentielles (nourriture, santé, toit)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = FinancialCalculations.formatCurrency(essentialExpenses, currencySymbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Facultatives (loisirs, restaurants)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = FinancialCalculations.formatCurrency(optionalExpenses, currencySymbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                        }
                    }
                }
            }
        }
    }
}
