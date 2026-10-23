package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DebtType(val labelFr: String) {
    CREDIT_CARD("Carte de crédit"),
    AUTO_LOAN("Prêt automobile"),
    PERSONAL_LOAN("Prêt personnel"),
    STUDENT_LOAN("Prêt étudiant"),
    MORTGAGE("Hypothèque"),
    OTHER("Autre dette")
}

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val creditor: String,
    val type: DebtType = DebtType.PERSONAL_LOAN,
    val totalOriginalAmount: Double,
    val remainingBalance: Double,
    val annualInterestRate: Double, // e.g. 18.5 for 18.5%
    val minimumPayment: Double,
    val dueDayOfMonth: Int = 15,
    val notes: String = ""
)

@Entity(tableName = "debt_payments")
data class DebtPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val debtId: Long,
    val amount: Double,
    val principalAmount: Double,
    val interestAmount: Double,
    val dateMillis: Long,
    val accountId: Long? = null,
    val notes: String = ""
)
