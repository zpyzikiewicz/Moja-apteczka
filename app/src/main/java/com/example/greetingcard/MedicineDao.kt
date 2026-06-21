package com.example.greetingcard

import androidx.room.Dao
import androidx.room.Query

@Dao
interface MedicineDao {
    @Query("SELECT * FROM leki WHERE nazwa LIKE '%' || :szukaj || '%' LIMIT 50")
    suspend fun szukajLeku(szukaj: String): List<Medicine>

    @Query("SELECT * FROM leki WHERE id = :id LIMIT 1")
    suspend fun getLekById(id: Int): Medicine?
}