package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.BillDao
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.DebtDao
import com.example.data.local.dao.MemberDao
import com.example.data.local.dao.SavingsDao
import com.example.data.local.dao.TransactionDao
import com.example.data.model.Account
import com.example.data.model.Bill
import com.example.data.model.BudgetCategory
import com.example.data.model.Category
import com.example.data.model.Debt
import com.example.data.model.DebtPayment
import com.example.data.model.HouseholdMember
import com.example.data.model.SavingsContribution
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Account::class,
        Category::class,
        Transaction::class,
        BudgetCategory::class,
        Bill::class,
        SavingsGoal::class,
        SavingsContribution::class,
        Debt::class,
        DebtPayment::class,
        HouseholdMember::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun billDao(): BillDao
    abstract fun savingsDao(): SavingsDao
    abstract fun debtDao(): DebtDao
    abstract fun memberDao(): MemberDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "family_budget_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                // Populate accounts
                for (account in InitialData.accounts) {
                    database.accountDao().insertAccount(account)
                }
                // Populate categories
                database.categoryDao().insertCategories(InitialData.categories)
                // Populate members
                database.memberDao().insertMembers(InitialData.members)
                // Populate budget
                database.budgetDao().insertBudgetCategories(InitialData.getInitialBudgetCategories())
                // Populate goals
                database.savingsDao().insertGoals(InitialData.savingsGoals)
                // Populate debts
                database.debtDao().insertDebts(InitialData.debts)
                // Populate bills
                database.billDao().insertBills(InitialData.getInitialBills())
                // Populate transactions
                for (tx in InitialData.getInitialTransactions()) {
                    database.transactionDao().insertTransaction(tx)
                }
            }
        }
    }
}
