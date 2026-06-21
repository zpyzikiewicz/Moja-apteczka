package com.example.greetingcard

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_medicines")
data class UserMedicine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // obowiązkowe
    val nazwa: String,
    val dataWaznosci: String,

    // nieobowiązkowe
    val kategoria: String? = null,
    val ilosc: Int? = null,
    val jednostka: String? = null,
    val uwagi: String? = null,

    // jeśli wybrany z bazy leków
    val medicineId: Int? = null,
    val postac: String? = null,
    val substancjaCzynna: String? = null
)