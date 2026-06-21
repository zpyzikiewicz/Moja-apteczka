package com.example.greetingcard

import android.app.Application

class MyApplication : Application() {
    val medicineDatabase by lazy {
        MedicineDatabase.getInstance(this)
    }
    val userDatabase by lazy {
        UserDatabase.getInstance(this)
    }
}