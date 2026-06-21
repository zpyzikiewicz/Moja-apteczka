package com.example.greetingcard

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "leki")
data class Medicine(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id") val id: Int?,
    @ColumnInfo(name = "nazwa") val nazwa: String?,
    @ColumnInfo(name = "droga_podania") val drogaPodania: String?,
    @ColumnInfo(name = "moc") val moc: String?,
    @ColumnInfo(name = "postac") val postac: String?,
    @ColumnInfo(name = "substancja_czynna") val substancjaCzynna: String?,
    @ColumnInfo(name = "nazwa_wytworcy") val nazwaWytworcy: String?,
    @ColumnInfo(name = "kraj_wytworcy") val krajWytworcy: String?,
    @ColumnInfo(name = "ulotka") val ulotka: String?,
    @ColumnInfo(name = "charakterystyka") val charakterystyka: String?
)