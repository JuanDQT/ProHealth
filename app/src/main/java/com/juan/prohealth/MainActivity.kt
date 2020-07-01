package com.juan.prohealth

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.juan.prohealth.database.Control
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {

    val RANGO_AZUL: String = "rangoBajoAzul.json"
    val RANGO_ROJO: String = "rangoAltoRojo.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pintarValores()
        /**
         * Cuando introducimos una NUEVA lectura de Sangre aqui recalculamos los dias de control ( 4 o 7 )
         * validamos el campo nivel de sangre que recibimos y aplicamos los calculos
         * para seleccionar los valores de salida e imprimirlos
         */
        btnINR.setOnClickListener {

            doAskINR()
            return@setOnClickListener

        }

        btnEstadisticas.setOnClickListener {
            startActivity(Intent(this, BarCharActivity::class.java))
        }

        btnGmap.setOnClickListener(){

            if(comprabarSiExisteApp("com.google.android.apps.maps", getApplicationContext())){
                // Buscar farmacias de guardia cercanas a mi posicion usando APP existente
                val gmmIntentUri = Uri.parse("geo:0,0?q=farmacia+de+guardia")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                mapIntent.resolveActivity(packageManager)?.let {
                    startActivity(mapIntent)
                }
                //Manera 2º en caso de no tener gmaps ejecutar Activity
                // val intent = Intent(this, UbicacionActivity::class.java)
                //startActivity(intent)
            }else{
                Toast.makeText(this, "Se necesita tener instalado Google Maps", Toast.LENGTH_LONG).show()
            }

        }

    }

    fun doAskINR() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Introducir")
        builder.setMessage("Por favor, introduce un valor INR entre 1 y 7")
        var positiveButton: Button? = null

        val view: LinearLayout = layoutInflater.inflate(R.layout.ad_introducir_inr, null) as LinearLayout
        val editText = view.findViewById<EditText>(R.id.etValor)

        // Buscamos si hay historial..
        val ultimosControles = Control.getUltimosIRN()

        if (ultimosControles.count() > 0) {
            val layoutHistorico = view.findViewById<LinearLayout>(R.id.ll_historico)
            layoutHistorico.visibility = View.VISIBLE
            val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroup)

            for (x in 0 until ultimosControles.count()) {
                val chip = getLayoutInflater().inflate(R.layout.layout_chip_choice, chipGroup, false) as Chip
                chip.text = ultimosControles[x]
                chipGroup.addView(chip)

                chip.setOnClickListener {
                    editText.setText(chip.text)
                }
            }
        }

        editText.addTextChangedListener( object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                positiveButton?.let { button ->
                    AppContext.validarInputTextSangre(editText.text.toString())?.let {
                        button.isEnabled = true
                        return
                    }
                    button.isEnabled = false
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
        builder.setView(view)

        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialogInterface, i ->
            this.hideKeyboard()
            val valorSangreNumerico = editText.text.toString().replace(",", ".").toFloat()
            val nomFichero = getFicheroCorrespondiente(valorSangreNumerico)

            val nivelyDias: Map<String, Int> = this.getNivelCorrespondiente((valorSangreNumerico))
            val dataNiveles = AppContext.getNivelFromFichero(nomFichero, nivelyDias["nivel"].toString(), nivelyDias["dias"].toString())

            Timer("SettingUp", false).schedule(500) {
                runOnUiThread {
                    doAskPlanificacion(sangre = "${AppContext.validarInputTextSangre(editText.text.toString())}", nivel = nivelyDias["nivel"].toString(), dataNiveles = dataNiveles)
                }
            }
        })

        builder.create()
        positiveButton = builder.show().getButton(AlertDialog.BUTTON_POSITIVE) as Button?
        positiveButton?.isEnabled = false

    }

    fun doAskPlanificacion(sangre: String, nivel: String, dataNiveles: ArrayList<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Informacion")

        val view = layoutInflater.inflate(R.layout.ad_planificacion, null)
        val irnActual = view.findViewById<TextView>(R.id.tvIRN)
        val irnNew = view.findViewById<TextView>(R.id.tvIRNNew)

        irnActual.text = MySharedPreferences.shared.getSangre()
        irnNew.text = sangre

        if (!Control.any()) {
            val irnText = view.findViewById<TextView>(R.id.tvIRN_text)
            val frameIRN = view.findViewById<FrameLayout>(R.id.frame_IRN)
            irnText.visibility = View.GONE
            frameIRN.visibility = View.GONE
        }


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
            textView.text = if (dataNiveles[x].isNullOrEmpty()) "No toca" else dataNiveles[x]

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
            Control.registrarControlActual(dataNiveles, sangre.toFloat(), nivel.toInt())

            // TEST: comprobamos los grabados
            Realm.getDefaultInstance().use {
                val controles = it.where(Control::class.java).findAll()
                Log.e("LOG", "Total: " + controles.size)
                for(item in controles)
                    print(item.toString())
            }
            pintarValores()

        })

        builder.create()
        builder.show()
    }

    fun getImageNameByJSON(jsonData: String): String {
        when (jsonData) {
            "0" -> return "entero"
            "1/8" -> return "un_octavo"
            "1/4" -> return "un_cuarto"
            "1/2" -> return "medio"
            "3/4" -> return "tres_cuartos"
            "1" -> return "entero"
            "1+1/4" -> return "entero_un_cuarto"
            "1+1/2" -> return "entero_un_medio"
            "1+3/4" -> return "entero_tres_cuartos"
            "2" -> return "dos_enteros"
            "2+1/4" -> return "dos_enteros_un_cuarto"
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

    /**
     * Manejamos el boton ATRAS para devolverlo a Login/User en caso de no haber ningun
     * registro en SharedPreferences
     * //TODO Crashea al volver para atras ya teniendo datos en SharedPreference y
     * Hipotetico usuario invitado
     */
    override fun onBackPressed() {

        if(!MySharedPreferences.shared.exists(arrayOf("nivel", "sangre"))){
            var intent = Intent(this, PreinicioActivity::class.java)
            startActivity(intent)
        }
    }

    fun comprabarSiExisteApp(nombrePaquete: String, context: Context): Boolean{
        val pm = context.packageManager
        return try {
            pm.getPackageInfo(nombrePaquete, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
