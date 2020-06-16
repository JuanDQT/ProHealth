package com.juan.prohealth.database

import com.juan.prohealth.MySharedPreferences
import com.juan.prohealth.addDays
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.createObject
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList

open class User : RealmObject() {

    @PrimaryKey
    private var id: String? = ""
    private var name: String? = ""

    companion object {
        fun crearUsuarioInvitado() {
            Realm.getDefaultInstance().executeTransactionAsync {
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

        fun setLogged(idUser: String) {
            // El id a guardar en el sharedpreferences sera el campo User.Id
            // Si en un futuro el usuario desea registrarse y no ser invitado, solamente tendra que actualizarse el campo User.id por el que le ofrezca el servidor
            MySharedPreferences.shared.addString(MySharedPreferences.shared.LOGGED_CURRENT_USER, idUser)
        }
    }

}