package com.example.greetingcard

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Medicine::class], version = 1)
abstract class MedicineDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao

    companion object {
        @Volatile
        private var INSTANCE: MedicineDatabase? = null

        fun getInstance(context: Context): MedicineDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MedicineDatabase::class.java,
                    "baza_lekow.db"
                )
                    .createFromAsset("baza_lekow.db")
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}