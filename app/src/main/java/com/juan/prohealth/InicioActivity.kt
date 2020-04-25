package com.juan.prohealth

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.et_sangre
import kotlinx.android.synthetic.main.inicio_main.*

class InicioActivity : AppCompatActivity() {

    val RANGO_AZUL: String = "rangoBajoAzul.json"
    val RANGO_ROJO: String = "rangoAltoRojo.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inicio_main)

        //pintarValores()
        /**
         * Cuando introducimos una NUEVA lectura de Sangre aqui recalculamos los dias de control ( 4 o 7 )
         * validamos el campo nivel de sangre que recibimos y aplicamos los calculos
         * para seleccionar los valores de salida e imprimirlos
         */
        btn_calcular.setOnClickListener {
            val inputValorSangreText = et_sangre.text.toString()
            if(AppContext.validarInputTextSangre(inputValorSangreText)){
                val valorSangreNumerico = inputValorSangreText.replace(",", ".").toFloat()
                if(valorSangreNumerico in 1.00..7.00){
                    val nomFichero = getFicheroCorrespondiente(valorSangreNumerico)

                    val nivelyDias: Map<String, Int> = getNivelCorrespondiente((valorSangreNumerico))

                    val dataNiveles = AppContext.getNivelFromFichero(nomFichero, nivelyDias["nivel"].toString(), nivelyDias["dias"].toString(), ((nivelyDias["nivel"]?:0)  == MySharedPreferences.shared.getNivel().toInt()))
                    val info =
                        "Nivel de sangre de control anterior: ${MySharedPreferences.shared.getSangre()}" +
                                "\nNivel de sangre actualizado: ${valorSangreNumerico}" +
                                "\nNivel de dosis de control anterior: ${MySharedPreferences.shared.getNivel()}" +
                                "\nNivel de dosis actualizado: ${nivelyDias["nivel"]}" +
                                "\nDosis diarias de nivel actual: ${dataNiveles}" +
                                "\nPróximo control en dias: ${nivelyDias["dias"]}";
                    tvInfo.text = info

                    doAskPlanificacion(sangre = et_sangre.text.toString(), nivel = nivelyDias["nivel"].toString(), dataNiveles = dataNiveles)

                    //pintarValores()
                } else {
                    Toast.makeText(
                        this,
                        "Ha de ser un valor entre 1.00 y 7.00",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }
    }

    fun doAskPlanificacion(sangre: String, nivel: String, dataNiveles: ArrayList<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Informacion")

        val view = layoutInflater.inflate(R.layout.ad_planificacion, null)

        for (x in 0 until 7) {
//        for (x in dataNiveles.withIndex()) {
            val layout = view.findViewWithTag<LinearLayout>("l${x}")

            if (x >= dataNiveles.size) {
                layout.visibility = View.GONE
                continue
            }

            layout.visibility = View.VISIBLE

            // sobrescribimos valor
            val textView = view.findViewWithTag<TextView>("t${x}")
            textView.text = dataNiveles[x]

            // sobreescribimos imagen
            if (!dataNiveles[x].isNullOrEmpty()) {
                val resID = resources.getIdentifier(getImageNameByJSON(dataNiveles[x]),"drawable", packageName)
                val imageView = view.findViewWithTag<ImageView>("i${x}")
                imageView.setBackgroundResource(resID)
            }

        }
        builder.setView(view)

        builder.setPositiveButton("Planificar", DialogInterface.OnClickListener { dialogInterface, i ->
            // Actualizamos la sangre y nivel
            MySharedPreferences.shared.addString("sangre", sangre)
            MySharedPreferences.shared.addString("nivel", nivel)
        })

        builder.create()
        builder.show()
    }

    // TODO: cuando tengamos las imagenes sobrescribimos los nombres que faltan..
    fun getImageNameByJSON(jsonData: String): String {
        when (jsonData) {
            "0" -> return "entero"
            "1/8" -> return "un_octavo"
            "1/4" -> return "un_cuarto"
            "1/2" -> return "medio"
            "3/4" -> return "tres_cuartos"
            "1" -> return "entero"

            /*"1+1/4" -> return "entero"
            "1+1/2" -> return "entero"
            "1+3/4" -> return "entero"
            "2" -> return "entero"
            "2+1/4" -> return "entero"*/
            else  -> return ""
        }
    }

    fun pintarValores() {
        tvSangre.text = "Nivel de sangre: ${MySharedPreferences.shared.getSangre()}"
        tvNivel.text = "Nivel de dosis: ${MySharedPreferences.shared.getNivel()}"
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
     * En funcion del nivel de sangre como parametro recibidoharemos una operacion de suma, resta o simplemente nada
     * segun el rango al que pertenezca este numero float devolvemos un Map[nivel,dias] Modificado - Aqui solo se vera afectada
     * el campo nivel ( suma - resta o dejado igual)
     */
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

    /**
     * Obtenemos el valor del JSON que hemos
     * guardado en SharedPreferences
     */
    fun getNivelActual(): String {
        return MySharedPreferences.shared.getNivel()
    }

}
