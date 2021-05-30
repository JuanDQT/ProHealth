package com.juan.prohealth.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juan.prohealth.database.entity.User

@Dao
interface UserDao {

    @Query("SELECT count(*) FROM user WHERE id = 0")
    suspend fun isInvitado(): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(user: User)

    @Query("select hour_alarm, minute_alarm from user where id = :id")
    fun getCurrentTimeNotification(id: Int): Array<Int>

    @Query("SELECT * FROM user WHERE id = :id")
    suspend fun getById(id: Int): User?

    @Query("select * from user")
    suspend fun getAll(): List<User>
}