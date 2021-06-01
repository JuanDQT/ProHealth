package com.juan.prohealth.database.daos

import android.util.Log
import androidx.room.*
import com.juan.prohealth.AppContext
import com.juan.prohealth.addDays
import com.juan.prohealth.clearTime
import com.juan.prohealth.customFormat
import com.juan.prohealth.database.Control2
import com.juan.prohealth.database.entity.Control
import com.juan.prohealth.database.entity.User
import io.realm.Realm
import java.lang.Exception
import java.util.*

@Dao
interface ControlDao {

    @Query("update control set medicado = 0 where medicado = 0 and execution_date < date()")
    suspend fun closeOldControls()

    @Query("select count(*) from control where date() >= execution_date and medicado = 0")
    fun getNumberPendingControls(): Int

    @Query("select count(*) from control where execution_date = date() and medicado = 0")
    fun getNumberOfControlsToday(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(user: Control)

    @Query("select * from user")
    suspend fun getAll(): List<User>
}


