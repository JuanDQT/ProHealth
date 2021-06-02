package com.juan.prohealth.source

import com.juan.prohealth.database.entity.User

interface UserLocalDataSource {
    suspend fun crearUsuarioInvitado(user: User)
    suspend fun userAlreadyExists(idServidor: Int, nameUser: String)
    suspend fun isInvitado(): Boolean
    suspend fun getCurrentTimeNotification(): Array<Int>
    suspend fun getAll(): List<User>
}