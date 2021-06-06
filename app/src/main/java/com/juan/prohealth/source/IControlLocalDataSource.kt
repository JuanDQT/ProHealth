package com.juan.prohealth.source

import com.juan.prohealth.database.entity.Control
import com.juan.prohealth.database.entity.User
import java.util.ArrayList

interface IControlLocalDataSource {
    suspend fun updateStateToCloseControls(idUser: Int)
    suspend fun hasPendingControls(idUser: Int): Boolean
    suspend fun checkPendingControlToday(idUser: Int, hour: Int, minute: Int): String
    suspend fun updateCurrentControl(idUser: Int, value: Boolean)
    suspend fun getActiveControlList(idUser: Int, medicated: Boolean): List<Control>
    suspend fun deleteLastControlGroup(idUser: Int)
    suspend fun getLastBloodValues(idUser: Int): Array<String>
    suspend fun insert(control: Control)
}