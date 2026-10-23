package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Account
import com.example.data.model.Bill
import com.example.data.model.BudgetCategory
import com.example.data.model.Category
import com.example.data.model.Debt
import com.example.data.model.HouseholdMember
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.repository.BudgetRepository
import com.example.domain.AdvisorInsight
import com.example.domain.CategoryBudgetProgress
import com.example.domain.FinancialCalculations
import com.example.domain.MonthlyFinancialSummary
import com.example.domain.SmartAdvisor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppScreen(val titleFr: String, val iconName: String) {
    DASHBOARD("Tableau de bord", "dashboard"),
    TRANSACTIONS("Transactions", "receipt_long"),
    BUDGET("Budget", "pie_chart"),
    BILLS("Factures", "calendar_today"),
    SAVINGS("Épargne", "savings"),
    DEBTS("Dettes", "credit_card"),
    REPORTS("Rapports", "bar_chart"),
    ADVISOR("Conseiller", "psychology"),
    SETTINGS("Paramètres", "settings")
}

class BudgetViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currencySymbol = MutableStateFlow("$")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _selectedMonthYear = MutableStateFlow(getCurrentMonthYearString())
    val selectedMonthYear: StateFlow<String> = _selectedMonthYear.asStateFlow()

    val accounts: StateFlow<List<Account>> = repository.allAccounts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val categories: StateFlow<List<Category>> = repository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val bills: StateFlow<List<Bill>> = repository.allBills.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val savingsGoals: StateFlow<List<SavingsGoal>> = repository.allSavingsGoals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val debts: StateFlow<List<Debt>> = repository.allDebts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val members: StateFlow<List<HouseholdMember>> = repository.allMembers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _budgetCategories = MutableStateFlow<List<BudgetCategory>>(emptyList())
    val budgetCategories: StateFlow<List<BudgetCategory>> = _budgetCategories.asStateFlow()

    init {
        viewModelScope.launch {
            _selectedMonthYear.collect { my ->
                repository.getBudgetCategoriesForMonth(my).collect { list ->
                    _budgetCategories.value = list
                }
            }
        }
    }

    // Monthly Financial Summary
    val monthlySummary: StateFlow<MonthlyFinancialSummary> = combine(
        transactions,
        accounts,
        bills,
        debts,
        _selectedMonthYear
    ) { txList, accList, billList, debtList, monthYear ->
        val monthTxs = filterTransactionsForMonth(txList, monthYear)
        val totalAccBalance = accList.sumOf { it.currentBalance }
        val unpaidBills = billList.filter { it.status.name != "PAID" }.sumOf { it.amount }
        val remainingDebts = debtList.sumOf { it.remainingBalance }

        FinancialCalculations.computeMonthlySummary(
            transactions = monthTxs,
            accountsBalance = totalAccBalance,
            unpaidBillsSum = unpaidBills,
            debtsSum = remainingDebts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlyFinancialSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    )

    // Category Budget Progress
    val budgetProgress: StateFlow<List<CategoryBudgetProgress>> = combine(
        budgetCategories,
        transactions,
        _selectedMonthYear
    ) { bCats, txList, monthYear ->
        val monthTxs = filterTransactionsForMonth(txList, monthYear)
        val expensesByCategory = monthTxs
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryName }

        bCats.map { bCat ->
            val spent = expensesByCategory[bCat.categoryName]?.sumOf { it.amount } ?: 0.0
            val remaining = bCat.allocatedAmount - spent
            val pct = if (bCat.allocatedAmount > 0) (spent / bCat.allocatedAmount) * 100.0 else 0.0
            CategoryBudgetProgress(
                categoryId = bCat.categoryId,
                categoryName = bCat.categoryName,
                allocatedAmount = bCat.allocatedAmount,
                spentAmount = spent,
                remainingAmount = remaining,
                percentageUsed = pct,
                isOverBudget = spent > bCat.allocatedAmount
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Intelligent Insights
    val advisorInsights: StateFlow<List<AdvisorInsight>> = combine(
        transactions,
        bills,
        savingsGoals,
        debts,
        budgetProgress
    ) { txs, bList, sGoals, dList, bProg ->
        SmartAdvisor.generateInsights(txs, bList, sGoals, dList, bProg, _currencySymbol.value)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setCurrency(symbol: String) {
        _currencySymbol.value = symbol
    }

    fun setSelectedMonthYear(monthYear: String) {
        _selectedMonthYear.value = monthYear
    }

    // CRUD Actions
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun addOrUpdateBudgetCategory(categoryId: Long, categoryName: String, amount: Double) {
        viewModelScope.launch {
            val item = BudgetCategory(
                monthYear = _selectedMonthYear.value,
                categoryId = categoryId,
                categoryName = categoryName,
                allocatedAmount = amount
            )
            repository.insertOrUpdateBudgetCategory(item)
        }
    }

    fun deleteBudgetCategoryByCategoryId(categoryId: Long) {
        viewModelScope.launch {
            repository.deleteBudgetCategoryByCategoryId(_selectedMonthYear.value, categoryId)
        }
    }

    fun deleteBudgetCategory(budgetCategory: BudgetCategory) {
        viewModelScope.launch {
            repository.clearBudgetForMonth(_selectedMonthYear.value)
        }
    }

    fun addBill(bill: Bill) {
        viewModelScope.launch {
            repository.insertBill(bill)
        }
    }

    fun updateBill(bill: Bill) {
        viewModelScope.launch {
            repository.updateBill(bill)
        }
    }

    fun markBillPaid(bill: Bill, accountId: Long) {
        viewModelScope.launch {
            repository.markBillPaid(bill, accountId)
        }
    }

    fun deleteBill(bill: Bill) {
        viewModelScope.launch {
            repository.deleteBill(bill)
        }
    }

    fun addSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.insertGoal(goal)
        }
    }

    fun updateSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun addSavingsContribution(goalId: Long, amount: Double, accountId: Long?, isWithdrawal: Boolean, notes: String) {
        viewModelScope.launch {
            repository.addSavingsContribution(goalId, amount, accountId, isWithdrawal, notes)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun addDebt(debt: Debt) {
        viewModelScope.launch {
            repository.insertDebt(debt)
        }
    }

    fun recordDebtPayment(debtId: Long, totalPaid: Double, principalPaid: Double, interestPaid: Double, accountId: Long?, notes: String) {
        viewModelScope.launch {
            repository.recordDebtPayment(debtId, totalPaid, principalPaid, interestPaid, accountId, notes)
        }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }

    fun addAccount(account: Account) {
        viewModelScope.launch {
            repository.insertAccount(account)
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            repository.insertCategory(category)
        }
    }

    fun addMember(member: HouseholdMember) {
        viewModelScope.launch {
            repository.insertMember(member)
        }
    }

    fun deleteMember(member: HouseholdMember) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }

    fun exportTransactionsCsv(): String {
        return repository.generateCsvExport(transactions.value)
    }

    companion object {
        fun getCurrentMonthYearString(): String {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            return sdf.format(Date())
        }

        fun filterTransactionsForMonth(transactions: List<Transaction>, monthYear: String): List<Transaction> {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            return transactions.filter {
                val txMonth = sdf.format(Date(it.dateMillis))
                txMonth == monthYear
            }
        }
    }
}
