package com.juan.prohealth.database

import android.util.Log
import com.juan.prohealth.addDays
import com.juan.prohealth.clearTime
import com.juan.prohealth.withNoTime
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
                    control.fechaInicio = Date()
                    control.fecha = Date().addDays(x).clearTime()
                    control.fechaFin = Date().addDays(planificacion.size)
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

        fun hasControl(): Boolean {
            Realm.getDefaultInstance().use {
                val total = it.where(Control::class.java).count()

                if (total > 0) {
                    val data = it.where(Control::class.java).equalTo("fecha", Date().clearTime()).findFirst()
//                    val data = it.where(Control::class.java).equalTo("fecha", Date().withNoTime()).findFirst()
                    return data != null
                }
                return false;
            }
        }

        fun getHistoric(): List<Control> {
            Realm.getDefaultInstance().use {
                return it.copyFromRealm(it.where(Control::class.java).findAll()).distinctBy { it.fechaInicio?.date?.toUInt()  }
            }
        }

        // test
        fun getAll(): List<Control> {
            Realm.getDefaultInstance().use {
                return it.copyFromRealm(it.where(Control::class.java).findAll())
            }
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
    }

    override fun toString(): String {
        return "Control(id=$id, sangre=$sangre, nivelDosis=$nivelDosis, fecha=$fecha, fechaInicio=$fechaInicio, fechaFin=$fechaFin, recurso=$recurso)"
    }


}