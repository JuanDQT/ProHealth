package com.juan.prohealth.source

import com.juan.prohealth.database.room.User

interface IUserLocalDataSource {
    suspend fun create(user: User)
    suspend fun getIdCurrentUser(): Int
    suspend fun getCurrentUser(): User
    suspend fun getUserSuccessfulCreated(): Int
    suspend fun updateUserSchedule(hour: Int, minute: Int)
    suspend fun getDoseValue(): Int

    // Pending Update?
    suspend fun userAlreadyExists(idServidor: Int, nameUser: String)
    suspend fun isInvitado(): Boolean
    suspend fun getAll(): List<User>
    suspend fun getBloodValue(): Float
    suspend fun updateUser(user:User)
}