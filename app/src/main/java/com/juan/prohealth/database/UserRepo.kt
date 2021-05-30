package com.juan.prohealth.database

import com.juan.prohealth.MySharedPreferences
import com.juan.prohealth.database.daos.UserDao
import com.juan.prohealth.database.entity.User
import com.juan.prohealth.fromDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*
import kotlin.math.min

class UserRepo(var database: MyDatabase) {

    private var userDao: UserDao = database.userDao()

    suspend fun crearUsuarioInvitado() {
        val newUser = User(0, "", 0, 0, 0)
        withContext(Dispatchers.IO) {
            userDao.insert(newUser)
        }
    }

    suspend fun userAlreadyExists(idServidor: Int, nameUser: String) {
        val newUser = User(0, nameUser, 0, 0, idServidor)
        withContext(Dispatchers.IO) {
            userDao.insert(newUser)
        }
    }

    suspend fun isInvitado(): Boolean {
        // Registro con id -1 significa que es un invitado, y que aun no ha recibido id del servidor..
        return withContext(Dispatchers.IO) {
            userDao.isInvitado()
        }
    }

    // TODO: se deveria verificar por bbdd..
    fun isLogged(): Boolean {
        // Independientemente si esta registrado o es invitado
        return MySharedPreferences.shared.exists(arrayOf(MySharedPreferences.shared.LOGGED_CURRENT_USER))
    }

    suspend fun getCurrentTimeNotification(): Array<Int> {
        return withContext(Dispatchers.IO) {
             userDao.getCurrentTimeNotification(MySharedPreferences.shared.getString(
                MySharedPreferences.shared.LOGGED_CURRENT_USER))
        }
    }

    suspend fun settCurrentTimeNotification(hora: Int, minuto: Int): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userDao.getById(MySharedPreferences.shared.getString(
                MySharedPreferences.shared.LOGGED_CURRENT_USER))
            user?.let {
                it.hourAlarm = hora
                it.minuteAlamr = minuto
                userDao.update(user)
            }
        }
    }

    // TODO: Por bbdd...
    fun setLogged(idUser: String) {
        // El id a guardar en el sharedpreferences sera el campo User.Id
        // Si en un futuro el usuario desea registrarse y no ser invitado, solamente tendra que actualizarse el campo User.id por el que le ofrezca el servidor
        MySharedPreferences.shared.addString(MySharedPreferences.shared.LOGGED_CURRENT_USER, idUser)
    }

    suspend fun getAll(): List<User> {
        return withContext(Dispatchers.IO) {
            userDao.getAll()
        }
    }

    suspend fun isAlarmTime(): Boolean {
        val userTime: Array<Int> = getCurrentTimeNotification()

        val cal = Calendar.getInstance().fromDate(Date(), userTime[0], userTime[1])
        return Date().after(cal.time)
    }

}