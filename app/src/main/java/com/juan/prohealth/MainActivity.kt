package com.juan.prohealth

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ViewUtils
import com.andrognito.flashbar.Flashbar
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.gtomato.android.ui.transformer.FlatMerryGoRoundTransformer
import com.juan.prohealth.adapters.DosisAdapter
import com.juan.prohealth.database.Control
import com.juan.prohealth.database.User
import com.juan.prohealth.security.UniqueDeviceID
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.btnINR
import kotlinx.android.synthetic.main.activity_preinicio.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity(), View.OnClickListener {

    val RANGO_AZUL: String = "rangoBajoAzul.json"
    val RANGO_ROJO: String = "rangoAltoRojo.json"
    var flashBar: Flashbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnBorrar.setOnClickListener(this)
        btnINR.setOnClickListener(this)
        btnGmap.setOnClickListener(this)
        btnEstadisticas.setOnClickListener(this)
        btnCalendario.setOnClickListener(this)
        btnAjustes.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()


        // A nivel de codigo, tiene que guardar, si SPFecha es nula(primera vez) actualizamos SPFecha and continue
        // Si SPFecha existe(else) SPFeha == actual || SPFecha(+1 dia) == actual, actualizamos fecha, continue, else error + call

        // Primera vez
        val currentDate = MySharedPreferences.shared.getSystemDate()
        if (currentDate == 0.toLong()) {
            MySharedPreferences.shared.updateSystemDate(Date().clearTime().time)
        } else {
            if ((Date(currentDate) == Date().clearTime() || Date(currentDate).addDays(1) == Date().clearTime())) {
                MySharedPreferences.shared.updateSystemDate(Date().clearTime().time)

                // Tiempo uso expirado
                if (Date(currentDate) > Date(MySharedPreferences.shared.getFechaFinPrueba())) {

                    val pdLoading = ProgressDialog(this)
                    pdLoading.setMessage(getString(R.string.validando))
                    pdLoading.show()

                    alert(
                        "Alerta",
                        "Versión de prueba expirada",
                        "Aceptar",
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            finishAffinity()
                        },
                        "Verificar renovación",
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            // API
                            SyncData.validateDevice(object : JSONObjectRequestListener {
                                override fun onResponse(response: JSONObject?) {

                                    response?.let {
                                        val status = it.getInt("status")
                                        if (status == 1) {
                                            pdLoading.dismiss()

                                            val fechaFin = SimpleDateFormat("yyyy-MM-dd").parse(it.getString("fechaFin"))
                                            MySharedPreferences.shared.setFechaFinPrueba(fechaFin.clearTime().time)

                                            onResume()
                                        }
                                    }
                                }

                                override fun onError(anError: ANError?) {
                                    pdLoading.dismiss()
                                    alert(getString(R.string.alerta), getString(R.string.error_verificacion))
                                }
                            })

                        }, closable = false)
                }
            }
            else {
                alert("Alerta", "Por favor, no juege con las fechas. Vuelva a situarla a ${Date(currentDate).customFormat()}", "Aceptar", DialogInterface.OnClickListener { dialogInterface, i ->
                    finishAffinity()
                })
            }
        }

        Control.closeOlderControls()

        pintarValores()
        checkHasControlToday()

    }

    fun askForControl(valor: String?) {

        if (flashBar == null || (flashBar != null && !flashBar!!.isShown())) {
            flashBar =  Flashbar.Builder(this)
                .gravity(Flashbar.Gravity.BOTTOM)
                .title(getString(R.string.title_notificacion))
                .message(String.format(getString(R.string.msg_notificacion, valor)))
                .enableSwipeToDismiss()
                .backgroundColorRes(R.color.colorPrimaryDark)
                .positiveActionText("Si")
                .negativeActionText("Hoy no tomare")
                .positiveActionTextColorRes(R.color.colorAccent)
                .negativeActionTextColorRes(R.color.colorAccent)
                .positiveActionTapListener(object : Flashbar.OnActionTapListener {
                    override fun onActionTapped(bar: Flashbar) {
                        updateControlStatus(true)
                        bar.dismiss()
                    }
                })
                .negativeActionTapListener(object : Flashbar.OnActionTapListener {
                    override fun onActionTapped(bar: Flashbar) {
                        updateControlStatus(false)
                        bar.dismiss()
                    }
                })
                .build()

            flashBar?.show()
        }

    }

    // TODO: preguntar, si cuando un control acaba, ese mismo dia puede planificar nuevos controles, incluyendo el mismo dia? se daria el caso? si es superior mejor..
    fun updateControlStatus(status: Boolean) {
        Control.updateCurrentControl(status)
        pintarValores()
    }

    fun setDosisWidget() {
        carousel.visibility = View.VISIBLE
        val transformer = FlatMerryGoRoundTransformer()
        transformer.viewPerspective = 0.3

        transformer.farScale = -1.3

        carousel.transformer = transformer
        carousel.isInfinite = true
        val items = ArrayList(Control.getActiveControlList())
        carousel.adapter = DosisAdapter(items, applicationContext)

        if (items != null && items.count() > 0) {
            val position = items.indexOf(items.filter { f -> f.fecha == Date().clearTime() }.first())
            carousel.smoothScrollToPosition(position)
        }
    }

    fun checkHasControlToday() {
        if (Control.hasPendingControls()) {
            btnINR.isEnabled = false
            btnBorrar.isEnabled = true
            setDosisWidget()
            if(Control.hasControlToday() && User.isAlarmTime())
                askForControl(Control.getControlDay(Date())?.recurso)
            else flashBar?.dismiss()

        } else {
            btnINR.isEnabled = true
            btnBorrar.isEnabled = false
            carousel.visibility = View.GONE
            flashBar?.dismiss()
            MyWorkManager.clearAllWorks()
        }
    }

    // Controlamos eventos click Botones
    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                R.id.btnBorrar -> doAjustarIRN()
                R.id.btnINR -> doAskINR()
                R.id.btnGmap -> doOpenMaps()
                R.id.btnEstadisticas -> doOpenEstadisticas()
                R.id.btnCalendario -> doCalendario()
                R.id.btnAjustes -> startActivity(Intent(this, AjustesActivity::class.java))
            }
        }
    }

    fun doAjustarIRN() {
        val data = Control.getAll()
        Log.e("MainActivity", "ANTES")
        for(item in data)
            Log.e("MainActivity", item.toString())

        Control.restartIRN()
        checkHasControlToday()
        Log.e("MainActivity", "DESPUES")
        for(item in Control.getAll())
            Log.e("MainActivity", item.toString())
    }

    fun doOpenEstadisticas() {
        startActivity(Intent(this, BarCharActivity::class.java))
    }

    fun doOpenMaps() {
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

    /**
     * Cuando introducimos una NUEVA lectura de Sangre aqui recalculamos los dias de control ( 4 o 7 )
     * validamos el campo nivel de sangre que recibimos y aplicamos los calculos
     * para seleccionar los valores de salida e imprimirlos
     */
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

            if (nivelyDias["nivel"]!! < 1) {
                alert( message = "Consulte los datos con su MÉDICO")
                return@OnClickListener
            }
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
        val btnMails = view.findViewById<CheckBox>(R.id.cb_mails)

        irnActual.text = MySharedPreferences.shared.getSangre()
        irnNew.text = sangre

        if (!Control.any()) {
            val irnText = view.findViewById<TextView>(R.id.tvIRN_text)
            val frameIRN = view.findViewById<FrameLayout>(R.id.frame_IRN)
            irnText.visibility = View.GONE
            frameIRN.visibility = View.GONE
        }


        for (x in 0 until 7) {
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
                val imageView = view.findViewWithTag<ImageView>("i${x}")
                imageView.setBackgroundResource(AppContext.getImageNameByJSON(dataNiveles[x]))
            }

        }
        builder.setView(view)

        builder.setPositiveButton("Planificar", DialogInterface.OnClickListener { dialogInterface, i ->
            // Actualizamos la sangre y nivel
            MySharedPreferences.shared.addString("sangre", sangre)
            MySharedPreferences.shared.addString("nivel", nivel)
            Control.registrarControlActual(dataNiveles, sangre.toFloat(), nivel.toInt())
            checkHasControlToday()

            MyWorkManager.setWorkers(Control.getActiveControlList(onlyPendings = true))

            for(item in Control.getAll())
                Log.e("MainActivity", item.toString())

            pintarValores()

            if (btnMails.isChecked)
                sendEmailPlanificacion()

        })

        builder.create()
        builder.show()
    }

    fun sendEmailPlanificacion() {
        val data = Html.fromHtml(Control.getActiveControlListToEmail())
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", MySharedPreferences.shared.getString("emails"), null))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Planificacion IRN")
        emailIntent.putExtra(Intent.EXTRA_TEXT, data)
        startActivity(Intent.createChooser(emailIntent, "Enviar mail..."))
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
/*
    override fun onBackPressed() {
        return
        if(!MySharedPreferences.shared.exists(arrayOf("nivel", "sangre"))){
            var intent = Intent(this, PreinicioActivity::class.java)
            startActivity(intent)
        }
    }
*/

    fun comprabarSiExisteApp(nombrePaquete: String, context: Context): Boolean{
        val pm = context.packageManager
        return try {
            pm.getPackageInfo(nombrePaquete, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    fun doCalendario(){
        startActivity(Intent(this, CalendarioActivity::class.java))
    }
}
