package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDateMillis: Long,
    val iconName: String = "savings",
    val colorHex: String = "#006852",
    val notes: String = ""
)

@Entity(tableName = "savings_contributions")
data class SavingsContribution(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val amount: Double,
    val dateMillis: Long,
    val accountId: Long? = null,
    val isWithdrawal: Boolean = false,
    val notes: String = ""
)
