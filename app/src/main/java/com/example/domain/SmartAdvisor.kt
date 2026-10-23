package com.example.domain

import com.example.data.model.Bill
import com.example.data.model.BillStatus
import com.example.data.model.Debt
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import com.example.data.model.TransactionType

data class AdvisorInsight(
    val title: String,
    val description: String,
    val type: InsightType,
    val iconName: String
)

enum class InsightType {
    INFO,
    WARNING,
    SUCCESS,
    TIP
}

object SmartAdvisor {

    fun generateInsights(
        transactions: List<Transaction>,
        bills: List<Bill>,
        savingsGoals: List<SavingsGoal>,
        debts: List<Debt>,
        budgetProgressList: List<CategoryBudgetProgress>,
        currencySymbol: String = "$"
    ): List<AdvisorInsight> {
        val insights = mutableListOf<AdvisorInsight>()

        // 1. Alertes de budget
        val overBudgets = budgetProgressList.filter { it.isOverBudget }
        if (overBudgets.isNotEmpty()) {
            val names = overBudgets.joinToString(", ") { it.categoryName }
            insights.add(
                AdvisorInsight(
                    title = "Dépassement de budget détecté",
                    description = "Attention, vos dépenses dépassent le plafond prévu pour : $names. Réduisez les dépenses variables d'ici la fin du mois.",
                    type = InsightType.WARNING,
                    iconName = "warning"
                )
            )
        }

        val nearBudgets = budgetProgressList.filter { !it.isOverBudget && it.percentageUsed >= 80.0 }
        if (nearBudgets.isNotEmpty()) {
            val topNear = nearBudgets.first()
            insights.add(
                AdvisorInsight(
                    title = "Budget presque atteint (${topNear.percentageUsed.toInt()}%)",
                    description = "La catégorie « ${topNear.categoryName} » a déjà consommé ${topNear.percentageUsed.toInt()}% de son allocation (${FinancialCalculations.formatCurrency(topNear.spentAmount, currencySymbol)} dépensés).",
                    type = InsightType.INFO,
                    iconName = "pie_chart"
                )
            )
        }

        // 2. Factures à venir
        val now = System.currentTimeMillis()
        val sevenDaysFromNow = now + (7 * 24 * 3600 * 1000L)
        val upcomingSoon = bills.filter {
            it.status == BillStatus.PENDING && it.dueDateMillis in now..sevenDaysFromNow
        }
        if (upcomingSoon.isNotEmpty()) {
            val totalUpcoming = upcomingSoon.sumOf { it.amount }
            insights.add(
                AdvisorInsight(
                    title = "${upcomingSoon.size} facture(s) due(s) sous 7 jours",
                    description = "Prévoyez ${FinancialCalculations.formatCurrency(totalUpcoming, currencySymbol)} pour régler vos prochaines échéances (${upcomingSoon.joinToString { it.name }}).",
                    type = InsightType.INFO,
                    iconName = "receipt"
                )
            )
        }

        // 3. Analyse des dépenses alimentaires et restaurants
        val foodExpenses = transactions.filter {
            it.type == TransactionType.EXPENSE && (it.categoryName.contains("Courses", ignoreCase = true) || it.categoryName.contains("Alimentation", ignoreCase = true))
        }.sumOf { it.amount }

        val diningExpenses = transactions.filter {
            it.type == TransactionType.EXPENSE && it.categoryName.contains("Restaurant", ignoreCase = true)
        }.sumOf { it.amount }

        if (diningExpenses > 150.0) {
            insights.add(
                AdvisorInsight(
                    title = "Optimisation restaurants & sorties",
                    description = "Vous avez dépensé ${FinancialCalculations.formatCurrency(diningExpenses, currencySymbol)} en restaurants. Cuisiner en famille un repas de plus par semaine pourrait vous faire économiser environ 120 $ par mois.",
                    type = InsightType.TIP,
                    iconName = "restaurant"
                )
            )
        }

        // 4. Progression achat maison & projets d'épargne
        val houseGoal = savingsGoals.find { it.name.contains("Maison", ignoreCase = true) }
        if (houseGoal != null) {
            val remaining = houseGoal.targetAmount - houseGoal.currentAmount
            val monthlySavings = 400.0 // Estimation moyenne
            val months = FinancialCalculations.estimateMonthsToGoal(houseGoal.targetAmount, houseGoal.currentAmount, monthlySavings)
            if (months != null && months > 0) {
                insights.add(
                    AdvisorInsight(
                        title = "Objectif : Achat Maison",
                        description = "Il vous reste ${FinancialCalculations.formatCurrency(remaining, currencySymbol)} à épargner. En maintenant une contribution de 400 $ par mois, votre apport sera prêt dans environ $months mois !",
                        type = InsightType.SUCCESS,
                        iconName = "home"
                    )
                )
            }
        }

        // 5. Conseil dettes (Avantages méthode avalanche vs boule de neige)
        val activeDebts = debts.filter { it.remainingBalance > 0 }
        if (activeDebts.isNotEmpty()) {
            val highestInterest = activeDebts.maxByOrNull { it.annualInterestRate }
            if (highestInterest != null && highestInterest.annualInterestRate > 10.0) {
                insights.add(
                    AdvisorInsight(
                        title = "Stratégie de désendettement recommandée",
                        description = "Votre dette « ${highestInterest.creditor} » a un taux d'intérêt de ${highestInterest.annualInterestRate}%. Privilégiez son remboursement accéléré (méthode avalanche) pour stopper les frais d'intérêt élevés.",
                        type = InsightType.TIP,
                        iconName = "trending_down"
                    )
                )
            }
        }

        // 6. Conseil épargne de précaution
        val emergencyGoal = savingsGoals.find { it.name.contains("Urgence", ignoreCase = true) }
        if (emergencyGoal != null) {
            val pct = (emergencyGoal.currentAmount / emergencyGoal.targetAmount * 100).toInt().coerceIn(0, 100)
            insights.add(
                AdvisorInsight(
                    title = "Fonds d'urgence familial ($pct% complété)",
                    description = "Votre sécurité financière atteint ${FinancialCalculations.formatCurrency(emergencyGoal.currentAmount, currencySymbol)} sur un objectif de ${FinancialCalculations.formatCurrency(emergencyGoal.targetAmount, currencySymbol)}. Continuez ainsi !",
                    type = InsightType.SUCCESS,
                    iconName = "shield"
                )
            )
        }

        return insights
    }
}
