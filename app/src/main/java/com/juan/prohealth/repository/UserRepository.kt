package com.juan.prohealth.repository

import com.juan.prohealth.database.entity.User
import com.juan.prohealth.source.IUserLocalDataSource

class UserRepository(private var iUserLocalDataSource: IUserLocalDataSource) {

    suspend fun createUser(user: User) {
        iUserLocalDataSource.crearUsuarioInvitado(user)
    }

    suspend fun getCurrentTimeNotification(): Array<Int> {
        return iUserLocalDataSource.getTimeNotification()
    }

    suspend fun getIdCurrentUser(): Int {
        return iUserLocalDataSource.getIdCurrentUser()
    }

    suspend fun getBloodValue(): Float {
        return iUserLocalDataSource.getBloodValue()
    }

    suspend fun updateUserData(bloodValue: Float, level: Int) {
        return iUserLocalDataSource.updateUserData(bloodValue, level)
    }

}