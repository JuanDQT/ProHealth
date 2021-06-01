package com.juan.prohealth.source

import com.juan.prohealth.database.entity.User
import java.util.ArrayList

interface ControlLocalDataSource {
    suspend fun closeOldControls()
    suspend fun hasPendingControls(): Boolean
    suspend fun hasPendingControlToday(): Boolean
}