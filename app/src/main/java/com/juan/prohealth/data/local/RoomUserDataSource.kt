package com.juan.prohealth.data.local

import com.juan.prohealth.database.MyDatabase
import com.juan.prohealth.database.entity.User
import com.juan.prohealth.source.UserLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomUserDataSource(var database: MyDatabase) : UserLocalDataSource {

    var userDao = database.userDao()

    override suspend fun crearUsuarioInvitado(user: User) {
        withContext(Dispatchers.IO) {
            userDao.insert(user)
        }
    }

    override suspend fun userAlreadyExists(idServidor: Int, nameUser: String) {
        withContext(Dispatchers.IO) {
            TODO()
        }
    }

    override suspend fun isInvitado(): Boolean {
        return withContext(Dispatchers.IO) {
            userDao.isInvitado()
        }
    }

    override suspend fun getCurrentTimeNotification(): Array<Int> {
        return withContext(Dispatchers.IO) {
            TODO()
        }
    }

    override suspend fun getAll(): List<User> {
        return withContext(Dispatchers.IO) {
            userDao.getAll()
        }
    }
}