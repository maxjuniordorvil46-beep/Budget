package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AccountType
import com.example.data.model.BillFrequency
import com.example.data.model.BillStatus
import com.example.data.model.DebtType
import com.example.data.model.MemberRole
import com.example.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromAccountType(value: AccountType?): String? = value?.name

    @TypeConverter
    fun toAccountType(value: String?): AccountType? =
        value?.let { runCatching { AccountType.valueOf(it) }.getOrDefault(AccountType.OTHER) }

    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? =
        value?.let { runCatching { TransactionType.valueOf(it) }.getOrDefault(TransactionType.EXPENSE) }

    @TypeConverter
    fun fromBillFrequency(value: BillFrequency?): String? = value?.name

    @TypeConverter
    fun toBillFrequency(value: String?): BillFrequency? =
        value?.let { runCatching { BillFrequency.valueOf(it) }.getOrDefault(BillFrequency.MONTHLY) }

    @TypeConverter
    fun fromBillStatus(value: BillStatus?): String? = value?.name

    @TypeConverter
    fun toBillStatus(value: String?): BillStatus? =
        value?.let { runCatching { BillStatus.valueOf(it) }.getOrDefault(BillStatus.PENDING) }

    @TypeConverter
    fun fromDebtType(value: DebtType?): String? = value?.name

    @TypeConverter
    fun toDebtType(value: String?): DebtType? =
        value?.let { runCatching { DebtType.valueOf(it) }.getOrDefault(DebtType.OTHER) }

    @TypeConverter
    fun fromMemberRole(value: MemberRole?): String? = value?.name

    @TypeConverter
    fun toMemberRole(value: String?): MemberRole? =
        value?.let { runCatching { MemberRole.valueOf(it) }.getOrDefault(MemberRole.PARENT) }
}
