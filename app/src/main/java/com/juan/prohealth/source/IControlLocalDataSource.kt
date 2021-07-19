package com.juan.prohealth.source

import com.juan.prohealth.database.room.Control
import java.util.*

interface IControlLocalDataSource {
    suspend fun getActiveControlList(): List<Control>
    suspend fun deleteLastControlGroup(idUser: Int)
    suspend fun getLastBloodValues(idUser: Int): Array<Float>
    suspend fun insert(control: Control)
    suspend fun getAllPendingControls(): List<Control>
    suspend fun updateControl(control: Control)
    suspend fun getNewIdGroup():Int
    suspend fun getPendingControlToday():Control
    suspend fun checkPendingControlToday(isPending: Int):Boolean
}