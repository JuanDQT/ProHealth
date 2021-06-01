package com.juan.prohealth.data.local

import com.juan.prohealth.database.MyDatabase
import com.juan.prohealth.database.daos.ControlDao
import com.juan.prohealth.source.ControlLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomControlDataSource(var database: MyDatabase) : ControlLocalDataSource {

    private var controlDao: ControlDao = database.controlDao()

    // First
    override suspend fun closeOldControls() {
        withContext(Dispatchers.IO) {
            controlDao.closeOldControls()
        }
    }

    // Controles hoy, o manana, etc
    override suspend fun hasPendingControls(): Boolean {
        return withContext(Dispatchers.IO) {
            controlDao.getNumberPendingControls() > 0
        }
    }

    // Control pendiente de hoy
    override suspend fun hasPendingControlToday(): Boolean {
        return withContext(Dispatchers.IO) {
            controlDao.getNumberOfControlsToday() > 0
        }
    }
}