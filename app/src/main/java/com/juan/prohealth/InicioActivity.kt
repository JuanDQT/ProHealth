package com.juan.prohealth

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.et_sangre
import kotlinx.android.synthetic.main.inicio_main.*
import org.json.JSONObject
import java.nio.charset.StandardCharsets.UTF_8

class InicioActivity : AppCompatActivity() {

    val RANGO_AZUL: String = "rangoBajoAzul.json"
    val RANGO_ROJO: String = "rangoAltoRojo.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inicio_main)

        pintarValores()

        btn_calcular.setOnClickListener {

            // TODO: Controlar en este if que sea positivo y menor de 7.0
            if (!et_sangre.text.toString().isNullOrEmpty()) {
                if (et_sangre.text.toString().length <= 4) {
                    if (et_sangre.text.toString().contains(".") || et_sangre.text.toString().contains(",")) {
                        val valorIntroducidoDeSangre = et_sangre.text.toString().replace(",", ".").toFloat()
                        if (valorIntroducidoDeSangre > 1.0 && valorIntroducidoDeSangre <= 7.0) {
                            val nomFichero = getFicheroCorrespondiente(valorIntroducidoDeSangre)

                            val nivelyDias: Map<String, Int> = getNivelCorrespondiente((valorIntroducidoDeSangre))
                            val dataNiveles = AppContext.getNivelFromFichero(nomFichero, nivelyDias["nivel"].toString(), nivelyDias["dias"].toString(), ((nivelyDias["nivel"]?:0)  > MySharedPreferences.shared.getNivel().toInt()))
                            val info =
                                "Nivel de sangre actual: ${MySharedPreferences.shared.getSangre()}" +
                                        "\nNuevo nivel de sangre actual: ${valorIntroducidoDeSangre}" +
                                        "\nNivel actual: ${MySharedPreferences.shared.getNivel()}" +
                                        "\nNuevo nivel: ${nivelyDias["nivel"]}" +
                                        "\nDescripcion niveles: ${dataNiveles}" +
                                        "\nControl de dias: ${nivelyDias["dias"]}";
                            tvInfo.text = info

                            // Actualizamos la sangre y nivel
                            MySharedPreferences.shared.addString("sangre", et_sangre.text.toString())
                            MySharedPreferences.shared.addString("nivel", nivelyDias["nivel"].toString())
                            pintarValores()
                        } else {
                            Toast.makeText(
                                this,
                                "No se adminten valores negativos",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Es necesario que la cifra contenga punto o coma.(pj: nivel de sangre: 1,00)",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "El valor no puede ser superior a 4 digitos de tamaño",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(this, "El valor introducido no puede estar vacio", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    fun pintarValores() {
        tvSangre.text = "Nivel de sangre: ${MySharedPreferences.shared.getSangre()}"
        tvNivel.text = "Nivel de dosis: ${MySharedPreferences.shared.getNivel()}"
    }

    fun getFicheroCorrespondiente(valor: Float): String {
        if (valor >= 1.0 && valor <= 3.6) {
            return RANGO_AZUL
        } else return RANGO_ROJO
    }

    fun getNivelCorrespondiente(valor: Float): MutableMap<String, Int> {
        val map = mutableMapOf<String, Int>()
        when (valor) {
            // Azules
            in 1.0..1.5 -> {
                map["nivel"] = (getNivelActual().toInt() + 2)
                map["dias"] = 3
            }
            in 1.6..2.3 -> {
                map["nivel"] = (getNivelActual().toInt() + 1)
                map["dias"] = 4
            }
            in 2.4..3.6 -> {
                map["nivel"] = (getNivelActual().toInt())
                map["dias"] = 7
            }
            // Rojos
            in 3.7..4.9 -> {
                map["nivel"] = (getNivelActual().toInt() - 1)
                map["dias"] = 7
            }
            in 5.0..7.0 -> {
                map["nivel"] = (getNivelActual().toInt() - 2)
                map["dias"] = 4
            }
        }

        return map
    }

    fun getNivelActual(): String {
        return MySharedPreferences.shared.getNivel()
    }

}
