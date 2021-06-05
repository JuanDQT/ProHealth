package com.juan.prohealth.database

import com.juan.prohealth.MySharedPreferences
import com.juan.prohealth.ui.common.fromDate
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.lang.Exception
import java.util.*

open class User : RealmObject() {

    @PrimaryKey
    private var id: String? = ""
    private var name: String? = ""
    private var horaAlarma: Int = 20
    private var minutoAlarma: Int = 34

    companion object {
        fun crearUsuarioInvitado() {
            Realm.getDefaultInstance().executeTransaction {
                val user = it.createObject(User::class.java, "-1")
                user.name = "Invitado"
            }
        }

        fun userAlreadyExists(idServidor: String, nameUser: String) {
            Realm.getDefaultInstance().executeTransactionAsync {
                val user = it.createObject(User::class.java)
                user.id = idServidor
                user.name = nameUser
            }
        }

        fun isInvitado(): Boolean {
            // Registro con id -1 significa que es un invitado, y que aun no ha recibido id del servidor..
            Realm.getDefaultInstance().use {
                return it.where(User::class.java).equalTo("id", "-1").count() > 0
            }
        }

        fun isLogged(): Boolean {
            // Independientemente si esta registrado o es invitado
            return MySharedPreferences.shared.exists(arrayOf(MySharedPreferences.shared.LOGGED_CURRENT_USER))
        }

        fun getCurrentTimeNotification(): Array<Int> {
            Realm.getDefaultInstance().use {
                val currentUser =  it.where(User::class.java).equalTo("id", MySharedPreferences.shared.getString(MySharedPreferences.shared.LOGGED_CURRENT_USER)).findFirst()
                currentUser?.let {
                    return arrayOf(it.horaAlarma, it.minutoAlarma)
                }
            }
            return emptyArray()
        }

        fun settCurrentTimeNotification(hora: Int, minuto: Int): Boolean {
            return try {
                Realm.getDefaultInstance().executeTransaction {
                    val currentUser =  it.where(User::class.java).equalTo("id", MySharedPreferences.shared.getString(MySharedPreferences.shared.LOGGED_CURRENT_USER)).findFirst()
                    currentUser?.let {
                        currentUser.horaAlarma = hora
                        currentUser.minutoAlarma = minuto
                    }
                }
                true
            } catch (e: Exception) {
                false
            }
        }


        fun setLogged(idUser: String) {
            // El id a guardar en el sharedpreferences sera el campo User.Id
            // Si en un futuro el usuario desea registrarse y no ser invitado, solamente tendra que actualizarse el campo User.id por el que le ofrezca el servidor
            MySharedPreferences.shared.addString(MySharedPreferences.shared.LOGGED_CURRENT_USER, idUser)
        }

        fun getAll(): List<User> {
            Realm.getDefaultInstance().use {
                return it.copyFromRealm(it.where(User::class.java).findAll())
            }
        }

        fun isAlarmTime(): Boolean {
            val userTime: Array<Int> = getCurrentTimeNotification()

            val cal = Calendar.getInstance().fromDate(Date(), userTime[0], userTime[1])
            return Date().after(cal.time)
        }
    }

}