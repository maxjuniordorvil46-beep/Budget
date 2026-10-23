package com.example

import com.example.data.model.Debt
import com.example.data.model.DebtType
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.domain.FinancialCalculations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testMonthlySummaryCalculations() {
        val transactions = listOf(
            Transaction(
                id = 1,
                type = TransactionType.INCOME,
                amount = 3000.0,
                dateMillis = System.currentTimeMillis(),
                description = "Salaire",
                accountId = 1
            ),
            Transaction(
                id = 2,
                type = TransactionType.EXPENSE,
                amount = 1200.0,
                dateMillis = System.currentTimeMillis(),
                description = "Loyer",
                accountId = 1
            ),
            Transaction(
                id = 3,
                type = TransactionType.EXPENSE,
                amount = 300.0,
                dateMillis = System.currentTimeMillis(),
                description = "Courses",
                accountId = 1
            ),
            // Transfer to savings must NOT count as a consumption expense
            Transaction(
                id = 4,
                type = TransactionType.TRANSFER,
                amount = 450.0,
                dateMillis = System.currentTimeMillis(),
                description = "Virement épargne",
                accountId = 1,
                targetAccountId = 2
            )
        )

        val summary = FinancialCalculations.computeMonthlySummary(
            transactions = transactions,
            accountsBalance = 5000.0,
            unpaidBillsSum = 200.0,
            debtsSum = 1500.0
        )

        // Revenus = 3000
        assertEquals(3000.0, summary.totalIncome, 0.001)
        // Dépenses = 1200 + 300 = 1500 (le transfert de 450 n'est PAS une dépense)
        assertEquals(1500.0, summary.totalExpense, 0.001)
        // Solde = 3000 - 1500 = 1500
        assertEquals(1500.0, summary.monthlyBalance, 0.001)
        // Épargne transférée = 450
        assertEquals(450.0, summary.totalSavingsTransferred, 0.001)
        // Taux d'épargne = (450 / 3000) * 100 = 15.0%
        assertEquals(15.0, summary.savingsRate, 0.001)
    }

    @Test
    fun testDebtStrategiesAvalancheAndSnowball() {
        val debts = listOf(
            Debt(
                id = 1,
                creditor = "Prêt Auto",
                type = DebtType.AUTO_LOAN,
                totalOriginalAmount = 10000.0,
                remainingBalance = 5000.0,
                annualInterestRate = 5.0,
                minimumPayment = 200.0
            ),
            Debt(
                id = 2,
                creditor = "Carte Visa",
                type = DebtType.CREDIT_CARD,
                totalOriginalAmount = 2000.0,
                remainingBalance = 800.0,
                annualInterestRate = 19.5,
                minimumPayment = 50.0
            ),
            Debt(
                id = 3,
                creditor = "Prêt Personnel",
                type = DebtType.PERSONAL_LOAN,
                totalOriginalAmount = 4000.0,
                remainingBalance = 1500.0,
                annualInterestRate = 11.0,
                minimumPayment = 100.0
            )
        )

        // Avalanche: Highest interest rate first (Visa 19.5% -> Prêt Perso 11% -> Prêt Auto 5%)
        val avalanche = FinancialCalculations.calculateAvalancheStrategy(debts)
        assertEquals(3, avalanche.size)
        assertEquals("Carte Visa", avalanche[0].debt.creditor)
        assertEquals("Prêt Personnel", avalanche[1].debt.creditor)
        assertEquals("Prêt Auto", avalanche[2].debt.creditor)

        // Boule de neige (Snowball): Smallest balance first (Visa 800$ -> Prêt Perso 1500$ -> Prêt Auto 5000$)
        val snowball = FinancialCalculations.calculateSnowballStrategy(debts)
        assertEquals(3, snowball.size)
        assertEquals("Carte Visa", snowball[0].debt.creditor)
        assertEquals("Prêt Personnel", snowball[1].debt.creditor)
        assertEquals("Prêt Auto", snowball[2].debt.creditor)
    }

    @Test
    fun testSavingsEstimation() {
        // Target: 10 000$, Current: 4 000$, Monthly: 500$ -> Remaining 6000$ / 500$ = 12 months
        val months = FinancialCalculations.estimateMonthsToGoal(
            targetAmount = 10000.0,
            currentAmount = 4000.0,
            monthlyContribution = 500.0
        )
        assertNotNull(months)
        assertEquals(12, months)

        // If target reached, months = 0
        val monthsReached = FinancialCalculations.estimateMonthsToGoal(
            targetAmount = 5000.0,
            currentAmount = 5000.0,
            monthlyContribution = 500.0
        )
        assertEquals(0, monthsReached)
    }
}
