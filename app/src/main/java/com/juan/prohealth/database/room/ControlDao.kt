package com.juan.prohealth.database.room

import androidx.room.*
import java.util.*

@Dao
interface ControlDao {

    @Update
    suspend fun update(control: Control)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: Control)

    @Query("update control set medicated = 1 where medicated = 0 and execution_date < date() and user_id = :idUser")
    suspend fun updateStateToCloseControls(idUser: Int)

    @Query("select count(*) from control where date() >= execution_date and medicated = 0 and user_id = :idUser")
    suspend fun getNumberPendingControls(idUser: Int): Int

    @Query("select count(*) from control where execution_date = date() and medicated = 0 and user_id = :idUser")
    suspend fun getNumberOfControlsToday(idUser: Int): Int

    @Query("update control set medicated = :status where execution_date = date() and medicated = 0 and user_id = :idUser")
    suspend fun updatePendingControlToFinished(idUser: Int, status: Int)

    // Coge los dias del ultimo grupo de control
    @Query("SELECT * FROM control WHERE user_id = :idUser AND group_control = (SELECT group_control FROM control WHERE user_id = :idUser order by id limit 1)" )
    suspend fun getActiveControlList(idUser: Int): List<Control>

    @Query("DELETE FROM control WHERE group_control = (SELECT group_control FROM control WHERE user_id = :idUser order by id limit 1)")
    suspend fun deleteLastControlGroup(idUser: Int)

    @Query("SELECT distinct(blood) FROM control order by id desc limit :limit")
    suspend fun getLastBloodValues(limit: Int = 10): Array<Float>

    @Query("SELECT MAX(group_control) + 1 FROM control")
    suspend fun generateNewIdGroupFromDb(): Int?

    @Query("SELECT * FROM control")
    suspend fun getAllControl(): List<Control>

    @Query("SELECT * FROM control WHERE execution_date = :date")
    suspend fun getControlByDate(date:Date): Control

    @Query("SELECT COUNT(*) FROM control WHERE medicated = :isPending and  (execution_date = (CAST(strftime('%s', date())  AS  int) * 1000)) AND user_id = (SELECT id FROM user WHERE state_logging = 1)")
    suspend fun getNumberControlTodayWithSQuery(isPending: Int): Int

    @Query("SELECT * FROM control WHERE medicated = 0 and  (execution_date = (CAST(strftime('%s', date())  AS  int) * 1000)) AND user_id = (SELECT id FROM user WHERE state_logging = 1)")
    suspend fun getPendingControlToday(): Control

}