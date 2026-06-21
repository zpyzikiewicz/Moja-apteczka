package com.example.apteczka

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserMedicineDao {
    @Query("SELECT * FROM user_medicines ORDER BY dataWaznosci ASC")
    fun getAllMedicines(): Flow<List<UserMedicine>>

    @Query("SELECT * FROM user_medicines WHERE dataWaznosci >= :dzisiaj AND dataWaznosci <= :dataGraniczna ORDER BY dataWaznosci ASC")
    fun getWygasajaceLeki(dzisiaj: String, dataGraniczna: String): Flow<List<UserMedicine>>

    @Query("SELECT * FROM user_medicines WHERE nazwa LIKE '%' || :szukaj || '%' ORDER BY dataWaznosci ASC")
    fun szukajMoichLekow(szukaj: String): Flow<List<UserMedicine>>

    @Query("UPDATE user_medicines SET kategoria = :nowaNazwa WHERE kategoria = :staraNazwa")
    suspend fun updateKategoria(staraNazwa: String, nowaNazwa: String)

    @Query("SELECT * FROM user_medicines WHERE ',' || REPLACE(kategoria, ', ', ',') || ',' LIKE '%,' || :kategoria || ',%' ORDER BY dataWaznosci ASC")
    fun getLekiByKategoria(kategoria: String): Flow<List<UserMedicine>>

    @Query("SELECT * FROM user_medicines WHERE dataWaznosci BETWEEN :od AND :doData ORDER BY dataWaznosci ASC")
    fun getLekiByData(od: String, doData: String): Flow<List<UserMedicine>>

    @Query("SELECT * FROM user_medicines WHERE kategoria = :kategoria AND dataWaznosci BETWEEN :od AND :doData ORDER BY dataWaznosci ASC")
    fun getLekiByKategoriaIData(kategoria: String, od: String, doData: String): Flow<List<UserMedicine>>

    @Query("SELECT * FROM user_medicines WHERE dataWaznosci < :dzisiaj ORDER BY dataWaznosci ASC")
    fun getPrzeterminowaneLeki(dzisiaj: String): Flow<List<UserMedicine>>

    @Insert
    suspend fun insert(medicine: UserMedicine)

    @Delete
    suspend fun delete(medicine: UserMedicine)

    @Update
    suspend fun update(medicine: UserMedicine)
}

