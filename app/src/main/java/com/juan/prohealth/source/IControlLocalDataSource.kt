package com.juan.prohealth.source

import com.juan.prohealth.database.room.Control
import java.util.*

interface IControlLocalDataSource {
    suspend fun updateStateToCloseControls(idUser: Int)
    suspend fun hasPendingControls(idUser: Int): Boolean
    suspend fun updateCurrentControl(idUser: Int, value: Boolean)
    suspend fun getActiveControlList(idUser:Int): List<Control>
    suspend fun deleteLastControlGroup(idUser: Int)
    suspend fun getLastBloodValues(idUser: Int): Array<Float>
    suspend fun insert(control: Control)
    suspend fun getAll(): List<Control>
    suspend fun updateControl(control: Control)
    suspend fun getNewIdGroup():Int
    suspend fun getControlByDate(date: Date):Control
    suspend fun checkPendingControlToday(isPending: Int):Boolean
}