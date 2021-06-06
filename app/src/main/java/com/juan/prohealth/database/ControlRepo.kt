package com.juan.prohealth.database

import android.util.Log
import com.juan.prohealth.*
import com.juan.prohealth.database.daos.ControlDao
import com.juan.prohealth.database.daos.UserDao
import com.juan.prohealth.database.entity.User
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*
import kotlin.math.min

// TODO: ESTA CLASE NO SERVIRA. MIGRAR
class ControlRepo(var database: MyDatabase) {

    private var controlDao: ControlDao = database.controlDao()
    // First
    suspend fun closeOldControls() {
        withContext(Dispatchers.IO) {
            controlDao.closeOldControls()
        }
    }

    // Controles hoy, o manana, etc
    suspend fun hasPendingControls(): Boolean {
        return withContext(Dispatchers.IO) {
            controlDao.getNumberPendingControls() > 0
        }
    }

    // Control pendiente de hoy
    suspend fun hasPendingControlToday(): Boolean {
        return withContext(Dispatchers.IO) {
            controlDao.getNumberOfControlsToday() > 0
        }
    }




    fun registrarControlActual(planificacion: ArrayList<String>, sangre: Float, nivel: Int) {
        Realm.getDefaultInstance().use { realm ->
            for (x in 0 until planificacion.size) {
                realm.beginTransaction()
//                    val control: Control = realm.createObject(Control::class.java, UUID.randomUUID().toString())
                val control: Control2 = realm.createObject(Control2::class.java, getNextKey(realm))
                control.sangre = sangre
                control.nivelDosis = nivel
                control.recurso = planificacion[x]
                control.fechaInicio = Date().clearTime()
                control.fecha = Date().addDays(x).clearTime()
                control.fechaFin = Date().addDays(planificacion.size - 1).clearTime()
                realm.commitTransaction()
            }
        }
    }


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

    suspend fun getCurrentTimeNotification(): Array<Int> {
        return withContext(Dispatchers.IO) {
            userDao.getCurrentTimeNotification(MySharedPreferences.shared.getString(
                MySharedPreferences.shared.LOGGED_CURRENT_USER))
        }
    }

    suspend fun isAlarmTime(): Boolean {
        val userTime: Array<Int> = getCurrentTimeNotification()

        val cal = Calendar.getInstance().fromDate(Date(), userTime[0], userTime[1])
        return Date().after(cal.time)
    }

}

//

companion object {


    fun getUltimosIRN(limit: Int = 10): Array<String> {
        Realm.getDefaultInstance().use {
            val data = it.where(Control2::class.java).findAll().map { c -> c.sangre.toString() }.distinct().takeLast(limit)
            return data.toTypedArray()
        }
    }

    fun any(): Boolean {
        Realm.getDefaultInstance().use {
            val data = it.where(Control2::class.java).findFirst()
            return data != null
        }
    }


    fun updateCurrentControl(status: Boolean) {
        Realm.getDefaultInstance().executeTransaction {
            val data = it.where(Control2::class.java).equalTo("fecha", Date().clearTime()).and().isNull("medicado").findFirst()
            data?.let {
                data.medicado = status
            }
        }
    }

    // Se usa para la grafica..
    fun getHistoric(): List<Control2> {
        Realm.getDefaultInstance().use {
            return it.copyFromRealm(it.where(Control2::class.java).findAll()).distinctBy { it.fechaInicio?.date?.toUInt()  }
        }
    }

    fun getAll(): List<Control2> {
        Realm.getDefaultInstance().use {
            return it.copyFromRealm(it.where(Control2::class.java).findAll())
        }
    }

    fun getActiveControlList(onlyPendings: Boolean = false): List<Control2> {
        Realm.getDefaultInstance().use {
            val index = it.where(Control2::class.java).equalTo("fecha", Date().clearTime()).findFirst()
            if(onlyPendings)
                return it.copyFromRealm(it.where(Control2::class.java).equalTo("fechaInicio", index?.fechaInicio).and().isNull("medicado").findAll())
            else
                return it.copyFromRealm(it.where(Control2::class.java).equalTo("fechaInicio", index?.fechaInicio).findAll())
        }
    }

    fun getActiveControlListToEmail(): String {
        val items = this.getActiveControlList()
        if (items.count() > 0) {
            var body = "<h1>Control IRN</h1><br/><br/>"
            for (x in items) {
                body += "<p>Fecha: ${x.fecha?.customFormat("dd/MM/yyyy")}. Dosis: ${x.recurso}</p><br/>"
            }
            return body
        } else return "No hay datos"
    }

    fun exportDataMail(): String {
        val template = AppContext.getFileContentFromAssets("report_all.html")

        template?.let {
            val items = this.getAll()
            var content = it
            content = content.replace("{fecha}", Date().customFormat("dd/MM/yyyy"))
            if (items.count() > 0) {
                var fill = ""
                var isNewGroup = false
                var ultimaFechaInicio: Date? = null

                for ((index, control) in items.withIndex()) { // o si es nuevo

                    isNewGroup = if(ultimaFechaInicio == null || ultimaFechaInicio != control.fechaInicio) true else false

                    if (isNewGroup) {
                        fill += "<table class='generated'><caption>${control.fechaInicio?.customFormat("dd/MM/yyyy")} - ${control.fechaFin?.customFormat("dd/MM/yyyy")}</caption><tr><th>Fecha</th><th>Sangre</th><th>Dosis</th><th>Recurso</th><th>Medicado</th></tr>"
                    }
                    fill += "<tr><td>${control.fecha?.customFormat("dd/MM/yyyy")}</td><td>${control.sangre}</td><td>${control.nivelDosis}</td><td>${control.recurso}</td><td>${getMedicadoResult(control.medicado)}</td></tr>"

                    if(index == items.count() -1)
                        fill += "</table>"
                    ultimaFechaInicio = control.fechaInicio
                }
                return content.replace("{fillable}", fill)
            } else return it.replace("{fillable}", "No hay datos")

        }
        return "Error al cargar la plantilla"
    }

    fun getMedicadoResult(value: Boolean?): String {
        value?.let {
            if(it)
                return "Sí"
            else return "No"
        }
        return "No todavía"
    }

    fun restartIRN(): Boolean {
        return try {
            Realm.getDefaultInstance().use {
                val date = Date().clearTime()
                Log.e("MainActivity", date.toString())
                val toDelete =  it.where(Control2::class.java).greaterThanOrEqualTo("fecha", date).findAll()
                for (item in toDelete) {
                    it.beginTransaction()
                    item.deleteFromRealm()
                    it.commitTransaction()
                }
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    fun getControlDay(fecha: Date): Control2?{
        Realm.getDefaultInstance().use {
            var dataCheck = it.where(Control2::class.java).equalTo("fecha", fecha.clearTime()).findFirst()
            if(dataCheck!=null){
                return it.copyFromRealm(dataCheck)
            }
            return null
        }
    }
}
