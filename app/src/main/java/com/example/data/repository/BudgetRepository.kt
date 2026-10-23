package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.Account
import com.example.data.model.Bill
import com.example.data.model.BillStatus
import com.example.data.model.BudgetCategory
import com.example.data.model.Category
import com.example.data.model.Debt
import com.example.data.model.DebtPayment
import com.example.data.model.HouseholdMember
import com.example.data.model.SavingsContribution
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BudgetRepository(private val db: AppDatabase) {

    // Accounts
    val allAccounts: Flow<List<Account>> = db.accountDao().getAllAccounts()
    suspend fun insertAccount(account: Account) = db.accountDao().insertAccount(account)
    suspend fun updateAccount(account: Account) = db.accountDao().updateAccount(account)
    suspend fun deleteAccount(account: Account) = db.accountDao().deleteAccount(account)

    // Transactions
    val allTransactions: Flow<List<Transaction>> = db.transactionDao().getAllTransactions()
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>> = db.transactionDao().getRecentTransactions(limit)
    fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>> =
        db.transactionDao().getTransactionsBetween(startMillis, endMillis)

    suspend fun insertTransaction(transaction: Transaction): Long {
        val id = db.transactionDao().insertTransaction(transaction)

        // Adjust account balances based on transaction type
        when (transaction.type) {
            TransactionType.INCOME -> {
                db.accountDao().adjustBalance(transaction.accountId, transaction.amount)
            }
            TransactionType.EXPENSE -> {
                db.accountDao().adjustBalance(transaction.accountId, -transaction.amount)
            }
            TransactionType.TRANSFER -> {
                db.accountDao().adjustBalance(transaction.accountId, -transaction.amount)
                transaction.targetAccountId?.let { targetId ->
                    db.accountDao().adjustBalance(targetId, transaction.amount)
                }
            }
        }
        return id
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        db.transactionDao().deleteTransaction(transaction)

        // Reverse the account balance impact
        when (transaction.type) {
            TransactionType.INCOME -> {
                db.accountDao().adjustBalance(transaction.accountId, -transaction.amount)
            }
            TransactionType.EXPENSE -> {
                db.accountDao().adjustBalance(transaction.accountId, transaction.amount)
            }
            TransactionType.TRANSFER -> {
                db.accountDao().adjustBalance(transaction.accountId, transaction.amount)
                transaction.targetAccountId?.let { targetId ->
                    db.accountDao().adjustBalance(targetId, -transaction.amount)
                }
            }
        }
    }

    // Categories
    val allCategories: Flow<List<Category>> = db.categoryDao().getAllCategories()
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>> = db.categoryDao().getCategoriesByType(type)
    suspend fun insertCategory(category: Category) = db.categoryDao().insertCategory(category)
    suspend fun deleteCategory(category: Category) = db.categoryDao().deleteCategory(category)

    // Budget
    fun getBudgetCategoriesForMonth(monthYear: String): Flow<List<BudgetCategory>> =
        db.budgetDao().getBudgetCategoriesForMonth(monthYear)

    suspend fun insertOrUpdateBudgetCategory(budgetCategory: BudgetCategory) =
        db.budgetDao().insertOrUpdateBudgetCategory(budgetCategory)

    suspend fun insertBudgetCategories(list: List<BudgetCategory>) =
        db.budgetDao().insertBudgetCategories(list)

    suspend fun clearBudgetForMonth(monthYear: String) =
        db.budgetDao().clearBudgetForMonth(monthYear)

    // Bills
    val allBills: Flow<List<Bill>> = db.billDao().getAllBills()
    val unpaidBills: Flow<List<Bill>> = db.billDao().getUnpaidBills()
    suspend fun insertBill(bill: Bill) = db.billDao().insertBill(bill)
    suspend fun updateBill(bill: Bill) = db.billDao().updateBill(bill)
    suspend fun deleteBill(bill: Bill) = db.billDao().deleteBill(bill)

    suspend fun markBillPaid(bill: Bill, accountId: Long) {
        val updated = bill.copy(
            status = BillStatus.PAID,
            paidAmount = bill.amount,
            paymentDateMillis = System.currentTimeMillis()
        )
        db.billDao().updateBill(updated)

        // Create matching expense transaction
        val tx = Transaction(
            type = TransactionType.EXPENSE,
            amount = bill.amount,
            dateMillis = System.currentTimeMillis(),
            categoryName = bill.categoryName,
            description = "Paiement facture : ${bill.name}",
            paymentMethod = bill.paymentMethod,
            accountId = accountId,
            memberName = "Famille",
            isFixed = true,
            isEssential = true,
            notes = bill.notes
        )
        insertTransaction(tx)
    }

    // Savings Goals
    val allSavingsGoals: Flow<List<SavingsGoal>> = db.savingsDao().getAllGoals()
    suspend fun insertGoal(goal: SavingsGoal) = db.savingsDao().insertGoal(goal)
    suspend fun updateGoal(goal: SavingsGoal) = db.savingsDao().updateGoal(goal)
    suspend fun deleteGoal(goal: SavingsGoal) = db.savingsDao().deleteGoal(goal)

    fun getContributionsForGoal(goalId: Long): Flow<List<SavingsContribution>> =
        db.savingsDao().getContributionsForGoal(goalId)

    suspend fun addSavingsContribution(
        goalId: Long,
        amount: Double,
        accountId: Long?,
        isWithdrawal: Boolean = false,
        notes: String = ""
    ) {
        val delta = if (isWithdrawal) -amount else amount
        db.savingsDao().adjustGoalAmount(goalId, delta)
        val contribution = SavingsContribution(
            goalId = goalId,
            amount = amount,
            dateMillis = System.currentTimeMillis(),
            accountId = accountId,
            isWithdrawal = isWithdrawal,
            notes = notes
        )
        db.savingsDao().insertContribution(contribution)

        // If an account is associated, adjust its balance
        accountId?.let { accId ->
            db.accountDao().adjustBalance(accId, -delta)
        }
    }

    // Debts
    val allDebts: Flow<List<Debt>> = db.debtDao().getAllDebts()
    suspend fun insertDebt(debt: Debt) = db.debtDao().insertDebt(debt)
    suspend fun updateDebt(debt: Debt) = db.debtDao().updateDebt(debt)
    suspend fun deleteDebt(debt: Debt) = db.debtDao().deleteDebt(debt)

    fun getPaymentsForDebt(debtId: Long): Flow<List<DebtPayment>> =
        db.debtDao().getPaymentsForDebt(debtId)

    suspend fun recordDebtPayment(
        debtId: Long,
        totalPaid: Double,
        principalPaid: Double,
        interestPaid: Double,
        accountId: Long?,
        notes: String = ""
    ) {
        db.debtDao().reduceDebtPrincipal(debtId, principalPaid)
        val payment = DebtPayment(
            debtId = debtId,
            amount = totalPaid,
            principalAmount = principalPaid,
            interestAmount = interestPaid,
            dateMillis = System.currentTimeMillis(),
            accountId = accountId,
            notes = notes
        )
        db.debtDao().insertPayment(payment)

        accountId?.let { accId ->
            db.accountDao().adjustBalance(accId, -totalPaid)
        }
    }

    // Household Members
    val allMembers: Flow<List<HouseholdMember>> = db.memberDao().getAllMembers()
    suspend fun insertMember(member: HouseholdMember) = db.memberDao().insertMember(member)
    suspend fun deleteMember(member: HouseholdMember) = db.memberDao().deleteMember(member)

    // Export CSV representation
    fun generateCsvExport(transactions: List<Transaction>): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("ID,Date,Type,Montant,Catégorie,Description,Mode de paiement,Membre,Fixe,Essentielle,Notes\n")
        for (tx in transactions) {
            val dateStr = sdf.format(Date(tx.dateMillis))
            sb.append("${tx.id},\"$dateStr\",${tx.type.labelFr},${tx.amount},\"${tx.categoryName}\",\"${tx.description.replace("\"", "\"\"")}\",\"${tx.paymentMethod}\",\"${tx.memberName}\",${tx.isFixed},${tx.isEssential},\"${tx.notes.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }
}
