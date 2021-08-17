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

    suspend fun getDoseValue(): Int {
        return iUserLocalDataSource.getDoseValue()
    }

    suspend fun updateUser(user: User) {
        return iUserLocalDataSource.updateUser(user)
    }

    suspend fun getCurrentUser(): User {
        return iUserLocalDataSource.getCurrentUser()
    }

    suspend fun getUserSuccesfulCreated(): Int {
        return iUserLocalDataSource.getUserSuccesfulCreated()
    }

    suspend fun updateUserSchedule(hour: Int, minute: Int) {
        iUserLocalDataSource.updateUserSchedule(hour, minute)
    }

}