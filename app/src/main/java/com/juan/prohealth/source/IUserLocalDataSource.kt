package com.juan.prohealth.source

import com.juan.prohealth.database.room.User

interface IUserLocalDataSource {
    suspend fun crearUsuarioInvitado(user: User)
    suspend fun getIdCurrentUser(): Int
    suspend fun getCurrentUser(): User

    // Pending Update?
    suspend fun userAlreadyExists(idServidor: Int, nameUser: String)
    suspend fun isInvitado(): Boolean
    suspend fun getAll(): List<User>
    suspend fun getBloodValue(): Float
    suspend fun updateUserData(bloodValue: Float, level: Int)
}