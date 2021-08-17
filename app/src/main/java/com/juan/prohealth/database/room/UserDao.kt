package com.juan.prohealth.database.room

import androidx.room.*

@Dao
interface UserDao {

    @Query("SELECT count(*) FROM user WHERE id = 0")
    suspend fun isInvitado(): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM user WHERE id = :id")
    suspend fun getById(id: Int): User?

    @Query("select * from user")
    suspend fun getAll(): List<User>

    @Query("SELECT id FROM user WHERE state_logging = 1")
    suspend fun getIdCurrentUser(): Int

    @Query("SELECT blood FROM user WHERE state_logging = 1")
    suspend fun getBloodValue(): Float

    @Query("SELECT level FROM user WHERE state_logging = 1")
    suspend fun getDoseValue(): Int

    @Query("UPDATE user set blood = :bloodValue, level = :level where state_logging = 1")
    suspend fun updateUserData(bloodValue: Float, level: Int)

    @Query("UPDATE user set hour_alarm = :hour, minute_alarm = :minute where state_logging = 1")
    suspend fun updateUserSchedule(hour: Int, minute: Int)

    @Query("SELECT * FROM user WHERE state_logging = 1")
    suspend fun getCurrentUser(): User

    @Query("SELECT count(*) FROM user WHERE state_logging = 1 and level <> 0")
    suspend fun getUserSuccesfulCreated(): Int

    @Update
    suspend fun update(user: User)
}