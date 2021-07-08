package com.juan.prohealth.repository

import com.juan.prohealth.database.room.Control
import com.juan.prohealth.source.IControlLocalDataSource
import java.util.*

class ControlRepository(private val iControlLocalDataSource: IControlLocalDataSource) {

    suspend fun updateStateToCloseControls(idUser: Int) {
        iControlLocalDataSource.updateStateToCloseControls(idUser)
    }

    suspend fun hasPendingControls(idUser: Int): Boolean {
        return iControlLocalDataSource.hasPendingControls(idUser)
    }

    suspend fun updateCurrentControl(value: Int, idUser: Int) {
        iControlLocalDataSource.updateCurrentControlToFinished(idUser, value)
    }

    suspend fun getActiveControlListByGroup(idUser: Int): List<Control> {
        return iControlLocalDataSource.getActiveControlList(idUser)
    }

    suspend fun deleteLastControlGroup(idUser: Int) {
        iControlLocalDataSource.deleteLastControlGroup(idUser)
    }

    suspend fun insert(control: Control) {
        iControlLocalDataSource.insert(control)
    }

    suspend fun updateControl(control: Control) {
        iControlLocalDataSource.updateControl(control)
    }

    suspend fun getNewIdGroup(): Int {
        return iControlLocalDataSource.getNewIdGroup()
    }

    suspend fun getPendingControlToday(): Control {
        return iControlLocalDataSource.getPendingControlToday()
    }

    suspend fun checkIfHasPendingControlToday(isPending: Int): Boolean {
        return iControlLocalDataSource.checkPendingControlToday(isPending)
    }
}