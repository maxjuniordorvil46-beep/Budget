package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BillFrequency(val labelFr: String) {
    ONCE("Unique"),
    WEEKLY("Hebdomadaire"),
    BIWEEKLY("Toutes les deux semaines"),
    MONTHLY("Mensuelle"),
    YEARLY("Annuelle")
}

enum class BillStatus(val labelFr: String) {
    PENDING("À payer"),
    PAID("Payée"),
    OVERDUE("En retard"),
    PARTIAL("Partiellement payée")
}

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val dueDateMillis: Long,
    val frequency: BillFrequency = BillFrequency.MONTHLY,
    val categoryName: String = "Logement",
    val status: BillStatus = BillStatus.PENDING,
    val paidAmount: Double = 0.0,
    val paymentDateMillis: Long? = null,
    val paymentMethod: String = "Virement",
    val notes: String = ""
)
