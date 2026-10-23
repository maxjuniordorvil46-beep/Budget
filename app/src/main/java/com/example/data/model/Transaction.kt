package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index("accountId"),
        Index("categoryId"),
        Index("dateMillis")
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val dateMillis: Long,
    val categoryId: Long? = null,
    val categoryName: String = "",
    val subCategory: String = "",
    val description: String,
    val paymentMethod: String = "Carte",
    val accountId: Long,
    val targetAccountId: Long? = null, // for TRANSFER
    val memberName: String = "Famille",
    val isFixed: Boolean = false,
    val isEssential: Boolean = true,
    val notes: String = "",
    val receiptUri: String? = null
)
