package com.juan.prohealth.repository

import com.juan.prohealth.source.ControlLocalDataSource

class ControlRepository(private val controlLocalDataSource: ControlLocalDataSource) {

    suspend fun closeOldControls() {
        controlLocalDataSource.closeOldControls()
    }

    suspend fun hasPendingControls(): Boolean {
        return controlLocalDataSource.hasPendingControls()
    }

    suspend fun hasPedingControlToday(): Boolean {
        return controlLocalDataSource.hasPendingControlToday()
    }
}