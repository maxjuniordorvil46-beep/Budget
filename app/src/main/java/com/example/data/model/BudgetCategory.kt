package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budget_categories",
    indices = [Index(value = ["monthYear", "categoryId"], unique = true)]
)
data class BudgetCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val monthYear: String, // e.g. "2026-10"
    val categoryId: Long,
    val categoryName: String,
    val allocatedAmount: Double
)
