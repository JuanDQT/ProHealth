package com.juan.prohealth.database

import android.util.Log
import com.juan.prohealth.addDays
import com.juan.prohealth.clearTime
import com.juan.prohealth.customFormat
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.lang.Exception
import java.util.*

open class Control : RealmObject() {

    @PrimaryKey private var id: Int? = null
    open var sangre: Float = 0f
    open var nivelDosis: Int = 0
    open var fecha: Date? = null
    open var fechaInicio: Date? = null
    open var fechaFin: Date? = null
    open var recurso: String? = null
    open var medicado: Boolean? = null

    companion object {

        fun getNextKey(realm: Realm): Int {
            return try {
                val number: Number? = realm.where(Control::class.java).max("id")

                if (number != null) {
                    number.toInt() + 1
                } else {
                    1
                }
            } catch (e: ArrayIndexOutOfBoundsException) {
                0
            }
        }
        fun registrarControlActual(planificacion: ArrayList<String>, sangre: Float, nivel: Int) {
            Realm.getDefaultInstance().use { realm ->
                for (x in 0 until planificacion.size) {
                    realm.beginTransaction()
//                    val control: Control = realm.createObject(Control::class.java, UUID.randomUUID().toString())
                    val control: Control = realm.createObject(Control::class.java, getNextKey(realm))
                    control.sangre = sangre
                    control.nivelDosis = nivel
                    control.recurso = planificacion[x]
                    control.fechaInicio = Date().clearTime()
                    control.fecha = Date().addDays(x).clearTime()
                    control.fechaFin = Date().addDays(planificacion.size).clearTime()
                    realm.commitTransaction()
                }
            }
        }

        fun getUltimosIRN(limit: Int = 10): Array<String> {
            Realm.getDefaultInstance().use {
                val data = it.where(Control::class.java).findAll().map { c -> c.sangre.toString() }.distinct().takeLast(limit)
                return data.toTypedArray()
            }
        }

        fun any(): Boolean {
            Realm.getDefaultInstance().use {
                val data = it.where(Control::class.java).findFirst()
                return data != null
            }
        }

        // Control pendiente de hoy
        fun hasControlToday(): Boolean {
            Realm.getDefaultInstance().use {
                val total = it.where(Control::class.java).count()

                if (total > 0) {
                    val data = it.where(Control::class.java).equalTo("fecha", Date().clearTime()).and().isNull("medicado").findFirst()
                    return data != null
                }
                return false;
            }
        }

        fun updateCurrentControl(status: Boolean) {
            Realm.getDefaultInstance().executeTransaction {
                val data = it.where(Control::class.java).equalTo("fecha", Date().clearTime()).and().isNull("medicado").findFirst()
                data?.let {
                    data.medicado = status
                }
            }
        }

        fun closeOlderControls() {
            Realm.getDefaultInstance().executeTransaction {
                val index = it.where(Control::class.java).equalTo("fecha", Date().clearTime()).findFirst()
                val olders = it.where(Control::class.java).equalTo("fechaInicio", index?.fechaInicio).and().lessThan("fecha", Date().clearTime()).and().isNull("medicado").findAll()
                for(x in olders)
                    x.medicado = false
            }
        }

        // Controles hoy, o manana, etc
        fun hasPendingControls(): Boolean {
            Realm.getDefaultInstance().use {
                val total = it.where(Control::class.java).count()
                if (total > 0) {
                    val data = it.where(Control::class.java).greaterThanOrEqualTo("fecha", Date().clearTime()).and().isNull("medicado").findFirst()
                    return data != null
                }
                return false;
            }
        }

        // Se usa para la grafica..
        fun getHistoric(): List<Control> {
            Realm.getDefaultInstance().use {
                return it.copyFromRealm(it.where(Control::class.java).findAll()).distinctBy { it.fechaInicio?.date?.toUInt()  }
            }
        }

        fun getAll(): List<Control> {
            Realm.getDefaultInstance().use {
                return it.copyFromRealm(it.where(Control::class.java).findAll())
            }
        }

        fun getActiveControlList(onlyPendings: Boolean = false): List<Control> {
            Realm.getDefaultInstance().use {
                val index = it.where(Control::class.java).equalTo("fecha", Date().clearTime()).findFirst()
                if(onlyPendings)
                    return it.copyFromRealm(it.where(Control::class.java).equalTo("fechaInicio", index?.fechaInicio).and().isNull("medicado").findAll())
                else
                    return it.copyFromRealm(it.where(Control::class.java).equalTo("fechaInicio", index?.fechaInicio).findAll())
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

        // De momento en html.. despues excel
        fun exportDataMail(): String {
            val items = this.getActiveControlList()
            if (items.count() > 0) {
                var body = "<h1>Historico controles</h1><br/><br/>"
                for (x in items) {
                    body += "<p><b>Fecha: ${x.fecha?.customFormat("dd/MM/yyyy")}</b>. Dosis: ${x.recurso}</p>Medicado: ${getMedicadoResult(x.medicado)}<br/>"
                }
                return body
            } else return "No hay datos"
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
                    val toDelete =  it.where(Control::class.java).greaterThanOrEqualTo("fecha", date).findAll()
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

        fun getControlDay(fecha: Date): Control?{
            Realm.getDefaultInstance().use {
                var dataCheck = it.where(Control::class.java).equalTo("fecha", fecha.clearTime()).findFirst()
                if(dataCheck!=null){
                    return it.copyFromRealm(dataCheck)
                }
               return null
            }
        }
    }

    override fun toString(): String {
        return "Control(id=$id, sangre=$sangre, nivelDosis=$nivelDosis, fecha=$fecha, fechaInicio=$fechaInicio, fechaFin=$fechaFin, recurso=$recurso)"
    }


}