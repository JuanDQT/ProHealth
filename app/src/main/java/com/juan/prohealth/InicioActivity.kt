package com.juan.prohealth

import android.content.Context
import android.os.Bundle
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
                val valorIntroducidoDeSangre = et_sangre.text.toString().toFloat()
                if (valorIntroducidoDeSangre > 1.0 && valorIntroducidoDeSangre <= 7.0) {
                    val nomFichero = getFicheroCorrespondiente(valorIntroducidoDeSangre)
                    val nivelyDias: Map<String, Int> = getNivelCorrespondiente(valorIntroducidoDeSangre)

                    // TODO: ya tenemos el map con los dias y nivel correspondiente a asignar.



                }
            }
        }

        val randomTest = openJSON()

        randomTest?.let {
            print(it)
            val json = JSONObject(it);
            val datosArray = json.getJSONArray("30")
//            Toast.makeText(this, "[30][0] " + datosArray[0], Toast.LENGTH_LONG).show()

        }
    }

    fun openJSON(): String? {
        var json: String? = null
        json = try {
            val inputStream = this.assets.open(RANGO_AZUL)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, UTF_8)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return json
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
