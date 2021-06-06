package com.juan.prohealth.repository

import com.juan.prohealth.database.entity.Control
import com.juan.prohealth.source.IControlLocalDataSource

class ControlRepository(private val iControlLocalDataSource: IControlLocalDataSource) {

    suspend fun updateStateToCloseControls(idUser: Int) {
        iControlLocalDataSource.updateStateToCloseControls(idUser)
    }

    suspend fun hasPendingControls(idUser: Int): Boolean {
        return iControlLocalDataSource.hasPendingControls(idUser)
    }

    suspend fun hasPedingControlToday(idUser: Int, time: Array<Int>): String {
        return iControlLocalDataSource.checkPendingControlToday(idUser, time[0], time[1])
    }

    suspend fun updateCurrentControl(value: Boolean, idUser: Int) {
        iControlLocalDataSource.updateCurrentControl(idUser, value)
    }

    suspend fun getActiveControlList(idUser: Int, value: Boolean): List<Control> {
        return iControlLocalDataSource.getActiveControlList(idUser, value)
    }

    suspend fun deleteLastControlGroup(idUser: Int) {
        iControlLocalDataSource.deleteLastControlGroup(idUser)
    }

    suspend fun insert(control: Control) {
        iControlLocalDataSource.insert(control)
    }
}