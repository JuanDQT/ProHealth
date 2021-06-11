package com.juan.prohealth.repository

import com.juan.prohealth.database.room.User
import com.juan.prohealth.source.IUserLocalDataSource

class UserRepository(private var iUserLocalDataSource: IUserLocalDataSource) {

    suspend fun createUser(user: User) {
        iUserLocalDataSource.crearUsuarioInvitado(user)
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

    suspend fun getCurrentUser(): User {
        return iUserLocalDataSource.getCurrentUser()
    }

}