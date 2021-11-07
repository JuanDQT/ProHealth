package com.juan.prohealth.database.room

import com.juan.prohealth.source.IControlLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomControlDataSource(var database: MyDatabase) : IControlLocalDataSource {

    private var controlDao: ControlDao = database.controlDao()

    override suspend fun getActiveControlList(): List<Control> {
        return withContext(Dispatchers.IO) {
            controlDao.getActiveControlList()
        }
    }

    override suspend fun deleteLastControlGroup() {
        return withContext(Dispatchers.IO) {
            controlDao.deleteLastControlsByGroup()
        }
    }

    override suspend fun getLastBloodValues(maxDose: Int): Array<Float> {
        return withContext(Dispatchers.IO) {
            controlDao.getLastBloodValues(maxDose)
        }
    }

    override suspend fun insert(control: Control) {
        return withContext(Dispatchers.IO) {
            controlDao.insert(control)
        }
    }

    // TODO: Esto no se llama en ningun sitio? DELETE
    override suspend fun getAllPendingControls(): List<Control> {
        return withContext(Dispatchers.IO) {
            controlDao.getAllPendingControls()
        }
    }

    override suspend fun updateControl(control: Control) {
        withContext(Dispatchers.IO) {
            controlDao.update(control)
        }
    }

    override suspend fun getNewIdGroup(): Int {
        return withContext(Dispatchers.IO) {
            controlDao.generateNewIdGroupFromDb() ?: 1
        }
    }

    override suspend fun getPendingControlToday(): Control {
        return withContext(Dispatchers.IO) {
            controlDao.getPendingControlToday()
        }
    }

    override suspend fun checkPendingControlToday(isPending: Int): Boolean {
        return withContext(Dispatchers.IO) {
            controlDao.getNumberControlTodayWithSQuery(isPending) > 0
        }
    }

    override suspend fun getControlListGraph(): List<Control> {
        return withContext(Dispatchers.IO) {
            controlDao.getControlListGraph()
        }
    }

    override suspend fun getAllControls(): List<Control> {
        return withContext(Dispatchers.IO) {
            controlDao.getAllControls()
        }
    }

    override suspend fun getLastControl(): Control? {
        return withContext(Dispatchers.IO) {
            controlDao.getLastControl()
        }
    }
}