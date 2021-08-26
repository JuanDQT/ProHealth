package com.juan.prohealth.repository

import com.juan.prohealth.database.room.Control
import com.juan.prohealth.source.IControlLocalDataSource

class ControlRepository(private val iControlLocalDataSource: IControlLocalDataSource) {

    suspend fun getActiveControlListByGroup(): List<Control> {
        return iControlLocalDataSource.getActiveControlList()
    }

    suspend fun deleteLastControlGroup() {
        iControlLocalDataSource.deleteLastControlGroup()
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

    suspend fun getLastBloodValues(maxDose: Int = 7): Array<Float> {
        return iControlLocalDataSource.getLastBloodValues(maxDose)
    }

    suspend fun getControlListGraph(): List<Control> {
        return iControlLocalDataSource.getControlListGraph()
    }
}