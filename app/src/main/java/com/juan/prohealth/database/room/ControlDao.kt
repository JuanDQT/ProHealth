package com.juan.prohealth.database.room

import androidx.room.*
import java.util.*

@Dao
interface ControlDao {

    @Update
    suspend fun update(control: Control)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: Control)

    @Query("SELECT * FROM control WHERE group_control = (SELECT group_control FROM control WHERE user_id = (SELECT id FROM user WHERE state_logging = 1) and (execution_date = (CAST(strftime('%s', date())  AS  int) * 1000)) order by id limit 1)" )
    suspend fun getActiveControlList(): List<Control>//Devuelve los controles PENDIENTES del ultimo grupo

    @Query("DELETE FROM control WHERE group_control = (SELECT max(group_control) FROM control WHERE user_id = (SELECT user_id FROM user WHERE state_logging = 1))")
    suspend fun deleteLastControlsByGroup()

    @Query("SELECT distinct(blood) FROM control order by id desc limit :limit")
    suspend fun getLastBloodValues(limit: Int): Array<Float>

    @Query("SELECT MAX(group_control) + 1 FROM control")
    suspend fun generateNewIdGroupFromDb(): Int?

    @Query("SELECT * FROM control WHERE medicated = -1 and resource <> '0' and  (execution_date = (CAST(strftime('%s', date())  AS  int) * 1000)) AND user_id = (SELECT id FROM user WHERE state_logging = 1)")
    suspend fun getAllPendingControls(): List<Control>//Nadie usa esta funcion pero creo que podria ser util

    @Query("SELECT * FROM control WHERE execution_date = :date AND user_id = (SELECT id FROM user WHERE state_logging = 1)")
    suspend fun getControlByDate(date: Date): Control

    // TODO: Revisar -> si el mismo dia se generan mas controles con fecha de hoy, se tendria que diferenciar por grupo, entonces cogeria bien: group_control = (SELECT group_control FROM control WHERE user_id = (SELECT id FROM user WHERE state_logging = 1))
    @Query("SELECT COUNT(*) FROM control WHERE medicated = :isPending and resource <> '0' and  (execution_date = (CAST(strftime('%s', date())  AS  int) * 1000)) AND user_id = (SELECT id FROM user WHERE state_logging = 1)")
    suspend fun getNumberControlTodayWithSQuery(isPending: Int): Int

    @Query("SELECT * FROM control WHERE medicated = -1 and resource <> '0' and  (execution_date = (CAST(strftime('%s', date())  AS  int) * 1000)) AND user_id = (SELECT id FROM user WHERE state_logging = 1)")
    suspend fun getPendingControlToday(): Control?

    @Query("SELECT * FROM control WHERE user_id = (SELECT id FROM user WHERE state_logging = 1) group by group_control")
    suspend fun getControlListGraph(): List<Control>

    @Query("SELECT * FROM control WHERE user_id = (SELECT id FROM user WHERE state_logging = 1)")
    suspend fun getAllControls(): List<Control>

    @Query("SELECT * FROM control WHERE user_id = (SELECT id FROM user WHERE state_logging = 1) ORDER BY group_control desc limit 1")
    suspend fun getLastControl(): Control?

}