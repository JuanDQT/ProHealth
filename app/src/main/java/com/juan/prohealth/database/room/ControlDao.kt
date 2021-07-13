package com.juan.prohealth.database.room

import androidx.room.*
import java.util.*

@Dao
interface ControlDao {

    @Update
    suspend fun update(control: Control)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: Control)

    @Query("SELECT * FROM control WHERE user_id = :idUser AND group_control = (SELECT group_control FROM control WHERE user_id = :idUser order by id limit 1)" )
    suspend fun getActiveControlList(idUser: Int): List<Control>//Devuelve controles TODOS group_control ??

    @Query("DELETE FROM control WHERE group_control = (SELECT group_control FROM control WHERE user_id = :idUser order by id limit 1)")
    suspend fun deleteLastControlsByGroup(idUser: Int)//Borra controles segun el group_control??

    @Query("SELECT distinct(blood) FROM control order by id desc limit :limit")
    suspend fun getLastBloodValues(limit: Int = 10): Array<Float>//No probado

    @Query("SELECT MAX(group_control) + 1 FROM control")
    suspend fun generateNewIdGroupFromDb(): Int?

    @Query("SELECT * FROM control WHERE medicated = 0")
    suspend fun getAllPendingControls(): List<Control>//By Group or By end_date

    @Query("SELECT * FROM control WHERE execution_date = :date")
    suspend fun getControlByDate(date:Date): Control

    @Query("SELECT COUNT(*) FROM control WHERE medicated = :isPending and  (execution_date = (CAST(strftime('%s', date())  AS  int) * 1000)) AND user_id = (SELECT id FROM user WHERE state_logging = 1)")
    suspend fun getNumberControlTodayWithSQuery(isPending: Int): Int

    @Query("SELECT * FROM control WHERE medicated = 0 and  (execution_date = (CAST(strftime('%s', date())  AS  int) * 1000)) AND user_id = (SELECT id FROM user WHERE state_logging = 1)")
    suspend fun getPendingControlToday(): Control
}