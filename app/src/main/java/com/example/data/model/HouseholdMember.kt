package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MemberRole(val labelFr: String) {
    CHEF_DE_FAMILLE("Chef de famille"),
    CONJOINT("Conjoint(e)"),
    PARENT("Parent"),
    ENFANT("Enfant"),
    AUTRE("Autre membre")
}

@Entity(tableName = "household_members")
data class HouseholdMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val role: MemberRole = MemberRole.PARENT,
    val avatarColorHex: String = "#006852"
)
