package com.juan.prohealth.database.room

import android.os.Parcelable
import androidx.room.*
import com.juan.prohealth.MySharedPreferences
import kotlinx.android.parcel.Parcelize
import java.util.*

/*
-1  No seteado(instancia)
0   No medicado(expresamente)
1   Medicado(expresamente)
 */
@Entity(foreignKeys = arrayOf(ForeignKey(entity = User::class,
    parentColumns = arrayOf("id"),
    childColumns = arrayOf("user_id"),
    onUpdate = ForeignKey.CASCADE,
    onDelete = ForeignKey.CASCADE)))
@Parcelize
data class Control(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "user_id")
    var idUser: Int = 0,
    var blood: Float = 0f,
    @ColumnInfo(name = "dose_level")
    var doseLevel: Int = 0,
    @ColumnInfo(name = "execution_date")
    var executionDate: Date = Calendar.getInstance().time,
    @ColumnInfo(name = "start_date")
    var startDate: Date = Calendar.getInstance().time,
    @ColumnInfo(name = "end_date")
    var endDate: Date = Calendar.getInstance().time,
    var resource: String = "",
    var medicated: Int = -1,
    @ColumnInfo(name = "group_control")
    var groupControl: Int = 0
): Parcelable {
    companion object {
        private val RANGO_AZUL: String = "rangoBajoAzul.json"
        private val RANGO_ROJO: String = "rangoAltoRojo.json"

        /**
         * En funcion del nivel de sangre como parametro recibidoharemos una operacion de suma, resta o simplemente nada
         * segun el rango al que pertenezca este numero float devolvemos un Map[nivel,dias] Modificado - Aqui solo se vera afectada
         * el campo nivel ( suma - resta o dejado igual)
         */
        fun getNivelCorrespondiente(inrInput: Float, currentDoseLevel: Int): MutableMap<String, Int> {
            val map = mutableMapOf<String, Int>()
            when (inrInput) {
                // Azules
                in 1.0..1.5 -> {
                    map["nivel"] = (currentDoseLevel + 2)
                    map["dias"] = 3
                }
                in 1.6..2.3 -> {
                    map["nivel"] = (currentDoseLevel + 1)
                    map["dias"] = 4
                }
                in 2.4..3.6 -> {
                    map["nivel"] = (currentDoseLevel)
                    map["dias"] = 7
                }
                // Rojos
                in 3.7..4.9 -> {
                    map["nivel"] = (currentDoseLevel - 1)
                    map["dias"] = 7
                }
                in 5.0..7.0 -> {
                    map["nivel"] = (currentDoseLevel - 2)
                    map["dias"] = 4
                }
            }

            return map
        }

        /**
         * Elejimos el JSON al que pertenece en funcion del rango
         * puede ser AZUL o ROJO.
         */

        fun getFicheroCorrespondiente(valor: Float): String {
            if (valor >= 1.0 && valor <= 3.6) {
                return RANGO_AZUL
            } else return RANGO_ROJO
        }

        /**
         * Obtenemos el valor del JSON que hemos
         * guardado en SharedPreferences
         */
        fun getCurrentDoseLevel(): String {
            return MySharedPreferences.shared.getNivel()
        }
    }
}
