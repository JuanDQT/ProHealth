package com.juan.prohealth.source

import com.juan.prohealth.database.entity.User

interface IUserLocalDataSource {
    suspend fun crearUsuarioInvitado(user: User)
    suspend fun getTimeNotification(): Array<Int>
    suspend fun getIdCurrentUser(): Int

    // Pending Update?
    suspend fun userAlreadyExists(idServidor: Int, nameUser: String)
    suspend fun isInvitado(): Boolean
    suspend fun getAll(): List<User>
    suspend fun getBloodValue(): Float
    suspend fun updateUserData(bloodValue: Float, level: Int)
}