package com.juan.prohealth.database.daos


import androidx.room.*
import com.juan.prohealth.database.entity.Control
import java.util.*

@Dao
interface ControlDao {

    @Query("update control set medicated = 1 where medicated = 0 and execution_date < date() and user_id = :idUser")
    suspend fun updateStateToCloseControls(idUser: Int)

    @Query("select count(*) from control where date() >= execution_date and medicated = 0 and user_id = :idUser")
    suspend fun getNumberPendingControls(idUser: Int): Int

    @Query("select count(*) from control where execution_date = date() and medicated = 0 and user_id = :idUser")
    suspend fun getNumberOfControlsToday(idUser: Int): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: Control)

    @Query("SELECT resource from control where user_id = :idUser and execution_date = date() and cast(strftime('%H', datetime()) as int) >= :hour and cast(strftime('%M', datetime()) as int) >= :minute")
    suspend fun checkPendingControlToday(hour: Int, minute: Int, idUser: Int): String

    @Query("update control set medicated = :status where execution_date = date() and medicated = 0 and user_id = :idUser")
    suspend fun updateCurrentControl(idUser: Int, status: Boolean)

    // Coge los dias del ultimo grupo de control
    @Query("SELECT * FROM control WHERE user_id = :idUser AND medicated = :medicated AND group_control = (SELECT group_control FROM control WHERE user_id = :idUser order by id limit 1)")
    suspend fun getActiveControlList(idUser: Int, medicated: Boolean): List<Control>

    @Query("DELETE FROM control WHERE group_control = (SELECT group_control FROM control WHERE user_id = :idUser order by id limit 1)")
    suspend fun deleteLastControlGroup(idUser: Int)

    @Query("SELECT distinct(blood) FROM control order by id desc limit :limit")
    suspend fun getLastBloodValues(limit: Int = 10): Array<Float>
}


