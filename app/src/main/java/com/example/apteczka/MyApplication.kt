package com.example.apteczka

import android.app.Application

class MyApplication : Application() {
    val medicineDatabase by lazy {
        MedicineDatabase.getInstance(this)
    }
    val userDatabase by lazy {
        UserDatabase.getInstance(this)
    }
}