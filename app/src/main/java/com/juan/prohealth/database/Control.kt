package com.juan.prohealth.database

import com.juan.prohealth.addDays
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
    private var sangre: Float = 0f
    private var nivelDosis: Int = 0
    private var fecha: Date? = null
    private var fechaInicio: Date? = null
    private var fechaFin: Date? = null
    private var recurso: String? = null

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
    }

}