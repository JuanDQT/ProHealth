package com.juan.prohealth.database

import android.util.Log
import com.juan.prohealth.addDays
import com.juan.prohealth.print
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList

open class Control : RealmObject() {

    @PrimaryKey private var id: String? = ""
    open var sangre: Float = 0f
    open var nivelDosis: Int = 0
    open var fecha: Date? = null
    open var fechaInicio: Date? = null
    open var fechaFin: Date? = null
    open var recurso: String? = null

    companion object {
        fun registrarControlActual(planificacion: ArrayList<String>, sangre: Float, nivel: Int) {
            Realm.getDefaultInstance().use { realm ->
                for (x in 0 until planificacion.size) {
                    realm.beginTransaction()
                    val control: Control = realm.createObject(Control::class.java, UUID.randomUUID().toString())
                    control.sangre = sangre
                    control.nivelDosis = nivel
                    control.recurso = planificacion[x]
                    control.fechaInicio = Date()
                    control.fecha = Date().addDays(x)
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
                    val data = it.where(Control::class.java).findAll().last()
                    data?.let {
                        if(Date().after(data.fechaInicio) && Date().before(data.fechaFin))
                            return true;
                    }
                }
                return false;
            }
        }

        fun getHistoric(): List<Control> {
            Realm.getDefaultInstance().use {
                return it.copyFromRealm(it.where(Control::class.java).findAll()).distinctBy { it.fechaInicio?.date?.toUInt()  }
            }
        }
    }

}