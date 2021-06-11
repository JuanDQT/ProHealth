package com.juan.prohealth.database.room

import com.juan.prohealth.source.IUserLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomUserDataSource(var database: MyDatabase) : IUserLocalDataSource {

    var userDao = database.userDao()


    override suspend fun crearUsuarioInvitado(user: User) {
        withContext(Dispatchers.IO) {
            userDao.insert(user)
        }
    }

    override suspend fun userAlreadyExists(idServidor: Int, nameUser: String) {
        withContext(Dispatchers.IO) {
            //userDao.user Revisar que hacia esto..
        }
    }

    override suspend fun isInvitado(): Boolean {
        return withContext(Dispatchers.IO) {
            userDao.isInvitado()
        }
    }

    override suspend fun getIdCurrentUser(): Int {
        return withContext(Dispatchers.IO) {
            userDao.getIdCurrentUser()
        }
    }

    override suspend fun getCurrentUser(): User {
        return withContext(Dispatchers.IO) {
            userDao.getCurrentUser()
        }
    }

    override suspend fun getAll(): List<User> {
        return withContext(Dispatchers.IO) {
            userDao.getAll()
        }
    }

    override suspend fun getBloodValue(): Float {
        return withContext(Dispatchers.IO) {
            userDao.getBloodValue()
        }
    }

    override suspend fun updateUserData(bloodValue: Float, level: Int) {
        return withContext(Dispatchers.IO) {
            userDao.updateUserData(bloodValue, level)
        }
    }
}