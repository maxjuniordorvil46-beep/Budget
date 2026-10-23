package com.example.domain

import com.example.data.model.Debt
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import java.text.NumberFormat
import java.util.Locale

data class MonthlyFinancialSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val monthlyBalance: Double,
    val totalSavingsTransferred: Double,
    val savingsRate: Double,
    val totalAccountsBalance: Double,
    val totalUnpaidBills: Double,
    val totalRemainingDebts: Double
)

data class CategoryBudgetProgress(
    val categoryId: Long,
    val categoryName: String,
    val allocatedAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val percentageUsed: Double,
    val isOverBudget: Boolean
)

data class DebtStrategyItem(
    val debt: Debt,
    val priorityRank: Int,
    val rationale: String
)

object FinancialCalculations {

    fun formatCurrency(amount: Double, symbol: String = "$"): String {
        val format = NumberFormat.getNumberInstance(Locale.FRANCE)
        format.minimumFractionDigits = 2
        format.maximumFractionDigits = 2
        return "${format.format(amount)} $symbol"
    }

    fun computeMonthlySummary(
        transactions: List<Transaction>,
        accountsBalance: Double,
        unpaidBillsSum: Double,
        debtsSum: Double
    ): MonthlyFinancialSummary {
        var income = 0.0
        var expense = 0.0
        var savingsTransfers = 0.0

        for (tx in transactions) {
            when (tx.type) {
                TransactionType.INCOME -> income += tx.amount
                TransactionType.EXPENSE -> expense += tx.amount
                TransactionType.TRANSFER -> {
                    // Transfers do not count as consumption expenses!
                    // If target account is savings (or savings in description), track as saved
                    savingsTransfers += tx.amount
                }
            }
        }

        val balance = income - expense
        val savingsRate = if (income > 0) {
            ((savingsTransfers / income) * 100.0).coerceIn(0.0, 100.0)
        } else 0.0

        return MonthlyFinancialSummary(
            totalIncome = income,
            totalExpense = expense,
            monthlyBalance = balance,
            totalSavingsTransferred = savingsTransfers,
            savingsRate = savingsRate,
            totalAccountsBalance = accountsBalance,
            totalUnpaidBills = unpaidBillsSum,
            totalRemainingDebts = debtsSum
        )
    }

    /**
     * Calcule la stratégie Boule de neige (Snowball) :
     * Priorise les dettes ayant les soldes restants les plus faibles.
     */
    fun calculateSnowballStrategy(debts: List<Debt>): List<DebtStrategyItem> {
        return debts
            .filter { it.remainingBalance > 0 }
            .sortedBy { it.remainingBalance }
            .mapIndexed { index, debt ->
                DebtStrategyItem(
                    debt = debt,
                    priorityRank = index + 1,
                    rationale = "Plus petit solde restant (${formatCurrency(debt.remainingBalance)}). Victoire psychologique rapide !"
                )
            }
    }

    /**
     * Calcule la stratégie Avalanche :
     * Priorise les dettes ayant les taux d'intérêt annuels les plus élevés.
     */
    fun calculateAvalancheStrategy(debts: List<Debt>): List<DebtStrategyItem> {
        return debts
            .filter { it.remainingBalance > 0 }
            .sortedByDescending { it.annualInterestRate }
            .mapIndexed { index, debt ->
                DebtStrategyItem(
                    debt = debt,
                    priorityRank = index + 1,
                    rationale = "Taux d'intérêt le plus lourd (${debt.annualInterestRate}%). Maximise l'économie sur les intérêts totaux !"
                )
            }
    }

    /**
     * Estimation du nombre de mois pour atteindre un objectif d'épargne
     */
    fun estimateMonthsToGoal(
        targetAmount: Double,
        currentAmount: Double,
        monthlyContribution: Double
    ): Int? {
        val remaining = targetAmount - currentAmount
        if (remaining <= 0) return 0
        if (monthlyContribution <= 0) return null
        return Math.ceil(remaining / monthlyContribution).toInt()
    }
}
