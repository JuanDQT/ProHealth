package com.quispe.coagutest.source

import com.quispe.coagutest.database.room.Control
import java.util.*

interface IControlLocalDataSource {
    suspend fun getActiveControlList(): List<Control>
    suspend fun deleteLastControlGroup()
    suspend fun getLastBloodValues(maxDose: Int): Array<Float>
    suspend fun insert(control: Control)
    suspend fun getAllPendingControls(): List<Control>
    suspend fun updateControl(control: Control)
    suspend fun getNewIdGroup(): Int
    suspend fun getPendingControlToday(): Control?
    suspend fun checkPendingControlToday(isPending: Int): Boolean
    suspend fun getControlListGraph(): List<Control>
    suspend fun getAllControls(): List<Control>
    suspend fun getLastControl(): Control?
    suspend fun getControlByDate(date: Date): Control?
}