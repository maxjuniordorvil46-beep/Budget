package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BudgetCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_categories WHERE monthYear = :monthYear ORDER BY allocatedAmount DESC")
    fun getBudgetCategoriesForMonth(monthYear: String): Flow<List<BudgetCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudgetCategory(budgetCategory: BudgetCategory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetCategories(categories: List<BudgetCategory>)

    @Delete
    suspend fun deleteBudgetCategory(budgetCategory: BudgetCategory)

    @Query("DELETE FROM budget_categories WHERE monthYear = :monthYear AND categoryId = :categoryId")
    suspend fun deleteBudgetCategoryByCategoryId(monthYear: String, categoryId: Long)

    @Query("DELETE FROM budget_categories WHERE monthYear = :monthYear")
    suspend fun clearBudgetForMonth(monthYear: String)
}
