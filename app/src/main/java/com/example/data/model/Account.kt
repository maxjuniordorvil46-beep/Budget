package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType(val labelFr: String) {
    CHECKING("Compte courant"),
    SAVINGS("Compte d'épargne"),
    CASH("Espèces"),
    CREDIT_CARD("Carte de crédit"),
    OTHER("Autre compte")
}

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val initialBalance: Double,
    val currentBalance: Double,
    val currency: String = "USD",
    val colorHex: String = "#006852"
)
