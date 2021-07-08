package com.juan.prohealth.database.room

import com.juan.prohealth.source.IControlLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class RoomControlDataSource(var database: MyDatabase) : IControlLocalDataSource {

    private var controlDao: ControlDao = database.controlDao()

    override suspend fun updateStateToCloseControls(idUser: Int) {
        withContext(Dispatchers.IO) {
            controlDao.updateStateToCloseControls(idUser)
        }
    }

    override suspend fun hasPendingControls(idUser: Int): Boolean {
        return withContext(Dispatchers.IO) {
            controlDao.getNumberPendingControls(idUser) > 0
        }
    }

    override suspend fun updateCurrentControl(idUser: Int, value: Boolean) {
        return withContext(Dispatchers.IO) {
            controlDao.updateCurrentControl(idUser, value)
        }
    }

    override suspend fun getActiveControlList(idUser: Int): List<Control> {
        return withContext(Dispatchers.IO) {
            controlDao.getActiveControlList(idUser)
        }
    }

    override suspend fun deleteLastControlGroup(idUser: Int) {
        return withContext(Dispatchers.IO) {
            controlDao.deleteLastControlGroup(idUser)
        }
    }

    override suspend fun getLastBloodValues(idUser: Int): Array<Float> {
        return withContext(Dispatchers.IO) {
            controlDao.getLastBloodValues(idUser)
        }
    }

    override suspend fun insert(control: Control) {
        return withContext(Dispatchers.IO) {
            controlDao.insert(control)
        }
    }

    override suspend fun getAll(): List<Control> {
        return withContext(Dispatchers.IO) {
            controlDao.getAllControl()
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

    override suspend fun getControlByDate(date: Date): Control {
        return withContext(Dispatchers.IO) {
            controlDao.getControlByDate(date)
        }
    }

    override suspend fun checkPendingControlToday(isPending: Int): Boolean {
        return withContext(Dispatchers.IO) {
            controlDao.getNumberControlTodayWithSQuery(isPending) > 0
        }
    }
}