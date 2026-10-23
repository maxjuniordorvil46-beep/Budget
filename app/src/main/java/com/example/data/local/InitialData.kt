package com.example.data.local

import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Bill
import com.example.data.model.BillFrequency
import com.example.data.model.BillStatus
import com.example.data.model.BudgetCategory
import com.example.data.model.Category
import com.example.data.model.Debt
import com.example.data.model.DebtType
import com.example.data.model.HouseholdMember
import com.example.data.model.MemberRole
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object InitialData {

    val accounts = listOf(
        Account(
            id = 1,
            name = "Compte Courant Principal",
            type = AccountType.CHECKING,
            initialBalance = 3200.0,
            currentBalance = 3450.0,
            colorHex = "#006852"
        ),
        Account(
            id = 2,
            name = "Livret Épargne Famille",
            type = AccountType.SAVINGS,
            initialBalance = 8500.0,
            currentBalance = 9000.0,
            colorHex = "#2E7D32"
        ),
        Account(
            id = 3,
            name = "Portefeuille Espèces",
            type = AccountType.CASH,
            initialBalance = 240.0,
            currentBalance = 180.0,
            colorHex = "#DDA100"
        ),
        Account(
            id = 4,
            name = "Carte Visa Remise",
            type = AccountType.CREDIT_CARD,
            initialBalance = 0.0,
            currentBalance = -450.0,
            colorHex = "#BA1A1A"
        )
    )

    val members = listOf(
        HouseholdMember(id = 1, name = "Marc", role = MemberRole.PARENT, avatarColorHex = "#1565C0"),
        HouseholdMember(id = 2, name = "Sophie", role = MemberRole.CONJOINT, avatarColorHex = "#AD1457"),
        HouseholdMember(id = 3, name = "Lucas", role = MemberRole.ENFANT, avatarColorHex = "#00897B")
    )

    val categories = listOf(
        // Revenus
        Category(id = 1, name = "Salaire principal", type = TransactionType.INCOME, iconName = "work", colorHex = "#2E7D32"),
        Category(id = 2, name = "Salaire conjoint", type = TransactionType.INCOME, iconName = "work", colorHex = "#388E3C"),
        Category(id = 3, name = "Allocations familiales", type = TransactionType.INCOME, iconName = "child_care", colorHex = "#00897B"),
        Category(id = 4, name = "Activité indépendante", type = TransactionType.INCOME, iconName = "business_center", colorHex = "#0288D1"),
        Category(id = 5, name = "Remboursements", type = TransactionType.INCOME, iconName = "replay", colorHex = "#7CB342"),
        Category(id = 6, name = "Autres revenus", type = TransactionType.INCOME, iconName = "attach_money", colorHex = "#558B2F"),

        // Dépenses
        Category(id = 7, name = "Loyer ou hypothèque", type = TransactionType.EXPENSE, iconName = "home", colorHex = "#E53935"),
        Category(id = 8, name = "Électricité, eau, gaz", type = TransactionType.EXPENSE, iconName = "bolt", colorHex = "#FB8C00"),
        Category(id = 9, name = "Internet et téléphone", type = TransactionType.EXPENSE, iconName = "wifi", colorHex = "#00ACC1"),
        Category(id = 10, name = "Courses alimentaires", type = TransactionType.EXPENSE, iconName = "shopping_cart", colorHex = "#43A047"),
        Category(id = 11, name = "Restaurants", type = TransactionType.EXPENSE, iconName = "restaurant", colorHex = "#F4511E"),
        Category(id = 12, name = "Transport et essence", type = TransactionType.EXPENSE, iconName = "directions_car", colorHex = "#5E35B1"),
        Category(id = 13, name = "Assurances", type = TransactionType.EXPENSE, iconName = "security", colorHex = "#3949AB"),
        Category(id = 14, name = "Santé et médicaments", type = TransactionType.EXPENSE, iconName = "medical_services", colorHex = "#D81B60"),
        Category(id = 15, name = "École et enfants", type = TransactionType.EXPENSE, iconName = "school", colorHex = "#8E24AA"),
        Category(id = 16, name = "Loisirs et sorties", type = TransactionType.EXPENSE, iconName = "sports_esports", colorHex = "#00897B"),
        Category(id = 17, name = "Abonnements", type = TransactionType.EXPENSE, iconName = "subscriptions", colorHex = "#1E88E5"),
        Category(id = 18, name = "Vêtements", type = TransactionType.EXPENSE, iconName = "checkroom", colorHex = "#6D4C41"),
        Category(id = 19, name = "Entretien maison", type = TransactionType.EXPENSE, iconName = "build", colorHex = "#546E7A"),
        Category(id = 20, name = "Frais bancaires", type = TransactionType.EXPENSE, iconName = "account_balance", colorHex = "#757575"),
        Category(id = 21, name = "Impôts et taxes", type = TransactionType.EXPENSE, iconName = "receipt_long", colorHex = "#C2185B"),
        Category(id = 22, name = "Remboursements dettes", type = TransactionType.EXPENSE, iconName = "credit_card", colorHex = "#B71C1C"),
        Category(id = 23, name = "Épargne projet", type = TransactionType.EXPENSE, iconName = "savings", colorHex = "#1B5E20"),
        Category(id = 24, name = "Autres dépenses", type = TransactionType.EXPENSE, iconName = "more_horiz", colorHex = "#607D8B")
    )

    fun getCurrentMonthYear(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getInitialBudgetCategories(monthYear: String = getCurrentMonthYear()): List<BudgetCategory> {
        return listOf(
            BudgetCategory(id = 1, monthYear = monthYear, categoryId = 7, categoryName = "Loyer ou hypothèque", allocatedAmount = 1300.0),
            BudgetCategory(id = 2, monthYear = monthYear, categoryId = 10, categoryName = "Courses alimentaires", allocatedAmount = 650.0),
            BudgetCategory(id = 3, monthYear = monthYear, categoryId = 12, categoryName = "Transport et essence", allocatedAmount = 280.0),
            BudgetCategory(id = 4, monthYear = monthYear, categoryId = 8, categoryName = "Électricité, eau, gaz", allocatedAmount = 180.0),
            BudgetCategory(id = 5, monthYear = monthYear, categoryId = 9, categoryName = "Internet et téléphone", allocatedAmount = 90.0),
            BudgetCategory(id = 6, monthYear = monthYear, categoryId = 14, categoryName = "Santé et médicaments", allocatedAmount = 150.0),
            BudgetCategory(id = 7, monthYear = monthYear, categoryId = 15, categoryName = "École et enfants", allocatedAmount = 200.0),
            BudgetCategory(id = 8, monthYear = monthYear, categoryId = 16, categoryName = "Loisirs et sorties", allocatedAmount = 220.0),
            BudgetCategory(id = 9, monthYear = monthYear, categoryId = 11, categoryName = "Restaurants", allocatedAmount = 180.0),
            BudgetCategory(id = 10, monthYear = monthYear, categoryId = 23, categoryName = "Épargne projet", allocatedAmount = 450.0)
        )
    }

    val savingsGoals = listOf(
        SavingsGoal(
            id = 1,
            name = "Fonds d'urgence familial (3 mois)",
            targetAmount = 6000.0,
            currentAmount = 4200.0,
            targetDateMillis = System.currentTimeMillis() + (120L * 24 * 3600 * 1000),
            iconName = "shield",
            colorHex = "#006852",
            notes = "Sécurité financière en cas d'imprévu"
        ),
        SavingsGoal(
            id = 2,
            name = "Apport achat maison",
            targetAmount = 25000.0,
            currentAmount = 8500.0,
            targetDateMillis = System.currentTimeMillis() + (500L * 24 * 3600 * 1000),
            iconName = "home",
            colorHex = "#1565C0",
            notes = "Apport personnel pour notre future résidence"
        ),
        SavingsGoal(
            id = 3,
            name = "Vacances d'été",
            targetAmount = 2200.0,
            currentAmount = 1400.0,
            targetDateMillis = System.currentTimeMillis() + (90L * 24 * 3600 * 1000),
            iconName = "beach_access",
            colorHex = "#FF8F00",
            notes = "Séjour détente en famille"
        )
    )

    val debts = listOf(
        Debt(
            id = 1,
            creditor = "Prêt Automobile Toyota",
            type = DebtType.AUTO_LOAN,
            totalOriginalAmount = 12000.0,
            remainingBalance = 3850.0,
            annualInterestRate = 4.9,
            minimumPayment = 245.0,
            dueDayOfMonth = 15,
            notes = "Prêt véhicule familial"
        ),
        Debt(
            id = 2,
            creditor = "Carte Visa Premier",
            type = DebtType.CREDIT_CARD,
            totalOriginalAmount = 1500.0,
            remainingBalance = 720.0,
            annualInterestRate = 18.9,
            minimumPayment = 65.0,
            dueDayOfMonth = 22,
            notes = "Paiement prioritaire pour éliminer intérêts élevés"
        ),
        Debt(
            id = 3,
            creditor = "Prêt Étudiant Sophie",
            type = DebtType.STUDENT_LOAN,
            totalOriginalAmount = 8000.0,
            remainingBalance = 2100.0,
            annualInterestRate = 3.2,
            minimumPayment = 110.0,
            dueDayOfMonth = 5,
            notes = "Solde final en bonne voie"
        )
    )

    fun getInitialBills(): List<Bill> {
        val now = System.currentTimeMillis()
        val day = 24 * 3600 * 1000L
        return listOf(
            Bill(
                id = 1,
                name = "Loyer mensuel",
                amount = 1300.0,
                dueDateMillis = now + (3 * day),
                frequency = BillFrequency.MONTHLY,
                categoryName = "Loyer ou hypothèque",
                status = BillStatus.PENDING,
                notes = "Virement automatique propriétaire"
            ),
            Bill(
                id = 2,
                name = "Facture Électricité EDF",
                amount = 112.50,
                dueDateMillis = now + (7 * day),
                frequency = BillFrequency.MONTHLY,
                categoryName = "Électricité, eau, gaz",
                status = BillStatus.PENDING,
                notes = "Consommation chauffage comprise"
            ),
            Bill(
                id = 3,
                name = "Abonnement Fibre & Forfaits",
                amount = 69.90,
                dueDateMillis = now + (11 * day),
                frequency = BillFrequency.MONTHLY,
                categoryName = "Internet et téléphone",
                status = BillStatus.PENDING,
                notes = "Box + 2 forfaits mobiles"
            ),
            Bill(
                id = 4,
                name = "Assurance Auto & Habitation",
                amount = 145.00,
                dueDateMillis = now - (2 * day),
                frequency = BillFrequency.MONTHLY,
                categoryName = "Assurances",
                status = BillStatus.PAID,
                paidAmount = 145.00,
                paymentDateMillis = now - (2 * day),
                notes = "Payé par prélèvement"
            )
        )
    }

    fun getInitialTransactions(): List<Transaction> {
        val now = System.currentTimeMillis()
        val day = 24 * 3600 * 1000L
        return listOf(
            // Revenus
            Transaction(
                id = 1,
                type = TransactionType.INCOME,
                amount = 3100.0,
                dateMillis = now - (18 * day),
                categoryId = 1,
                categoryName = "Salaire principal",
                description = "Salaire Marc mensuel",
                paymentMethod = "Virement",
                accountId = 1,
                memberName = "Marc",
                isFixed = true,
                isEssential = true
            ),
            Transaction(
                id = 2,
                type = TransactionType.INCOME,
                amount = 1850.0,
                dateMillis = now - (15 * day),
                categoryId = 2,
                categoryName = "Salaire conjoint",
                description = "Salaire Sophie temps partiel",
                paymentMethod = "Virement",
                accountId = 1,
                memberName = "Sophie",
                isFixed = true,
                isEssential = true
            ),
            Transaction(
                id = 3,
                type = TransactionType.INCOME,
                amount = 160.0,
                dateMillis = now - (10 * day),
                categoryId = 3,
                categoryName = "Allocations familiales",
                description = "Prestation CAF enfant",
                paymentMethod = "Virement",
                accountId = 1,
                memberName = "Famille",
                isFixed = true,
                isEssential = true
            ),

            // Dépenses
            Transaction(
                id = 4,
                type = TransactionType.EXPENSE,
                amount = 1300.0,
                dateMillis = now - (20 * day),
                categoryId = 7,
                categoryName = "Loyer ou hypothèque",
                description = "Paiement loyer appartement",
                paymentMethod = "Virement",
                accountId = 1,
                memberName = "Marc",
                isFixed = true,
                isEssential = true
            ),
            Transaction(
                id = 5,
                type = TransactionType.EXPENSE,
                amount = 184.20,
                dateMillis = now - (6 * day),
                categoryId = 10,
                categoryName = "Courses alimentaires",
                description = "Supermarché hebdomadaire",
                paymentMethod = "Carte",
                accountId = 1,
                memberName = "Sophie",
                isFixed = false,
                isEssential = true
            ),
            Transaction(
                id = 6,
                type = TransactionType.EXPENSE,
                amount = 125.60,
                dateMillis = now - (2 * day),
                categoryId = 10,
                categoryName = "Courses alimentaires",
                description = "Marché bio & fruits/légumes",
                paymentMethod = "Carte",
                accountId = 1,
                memberName = "Marc",
                isFixed = false,
                isEssential = true
            ),
            Transaction(
                id = 7,
                type = TransactionType.EXPENSE,
                amount = 75.0,
                dateMillis = now - (4 * day),
                categoryId = 12,
                categoryName = "Transport et essence",
                description = "Plein d'essence voiture",
                paymentMethod = "Carte",
                accountId = 1,
                memberName = "Marc",
                isFixed = false,
                isEssential = true
            ),
            Transaction(
                id = 8,
                type = TransactionType.EXPENSE,
                amount = 64.50,
                dateMillis = now - (1 * day),
                categoryId = 11,
                categoryName = "Restaurants",
                description = "Dîner pizzeria en famille",
                paymentMethod = "Carte",
                accountId = 1,
                memberName = "Famille",
                isFixed = false,
                isEssential = false
            ),
            Transaction(
                id = 9,
                type = TransactionType.EXPENSE,
                amount = 45.0,
                dateMillis = now - (3 * day),
                categoryId = 14,
                categoryName = "Santé et médicaments",
                description = "Pharmacie sirop et vitamines",
                paymentMethod = "Carte",
                accountId = 1,
                memberName = "Sophie",
                isFixed = false,
                isEssential = true
            ),
            // Transfert vers épargne (NE DOIT PAS ÊTRE COMPTÉ COMME DÉPENSE)
            Transaction(
                id = 10,
                type = TransactionType.TRANSFER,
                amount = 400.0,
                dateMillis = now - (12 * day),
                description = "Virement mensuel épargne précaution",
                paymentMethod = "Virement",
                accountId = 1,
                targetAccountId = 2,
                memberName = "Famille",
                isFixed = true,
                isEssential = true
            )
        )
    }
}
