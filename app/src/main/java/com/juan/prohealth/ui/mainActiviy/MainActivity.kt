package com.juan.prohealth.ui.mainActiviy

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
import androidx.lifecycle.ViewModelProvider
import com.andrognito.flashbar.Flashbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.gtomato.android.ui.transformer.FlatMerryGoRoundTransformer
import com.juan.prohealth.*
import com.juan.prohealth.data.local.RoomControlDataSource
import com.juan.prohealth.data.local.RoomUserDataSource
import com.juan.prohealth.data.local.SharedPreference
import com.juan.prohealth.data.local.StorageValidationDataSource
import com.juan.prohealth.database.Control2
import com.juan.prohealth.database.MyDatabase
import com.juan.prohealth.databinding.ActivityMainBinding
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.repository.UserRepository
import com.juan.prohealth.repository.ValidationRepository
import com.juan.prohealth.ui.adapters.DoseAdapter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var validationRepository: ValidationRepository
    private lateinit var controlRepository: ControlRepository
    private lateinit var userRepository:UserRepository
    private val RANGO_AZUL: String = "rangoBajoAzul.json"
    private val RANGO_ROJO: String = "rangoAltoRojo.json"
    var flashBar: Flashbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        buildDependencies()
        viewModel = buildViewModel()
        setContentView(binding.root)

        binding.btnBorrar.setOnClickListener(this)
        binding.btnINR.setOnClickListener(this)
        binding.btnGmap.setOnClickListener(this)
        binding.btnEstadisticas.setOnClickListener(this)
        binding.btnCalendario.setOnClickListener(this)
        binding.btnAjustes.setOnClickListener(this)

        subscribeUI()
    }

    private fun buildDependencies() {
        val sharedPreference = SharedPreference.getInstance(this.applicationContext)
        val database = MyDatabase.getDatabase(this)
        val controlLocal = RoomControlDataSource(database)
        val userLocal = RoomUserDataSource(database)

        validationRepository = ValidationRepository(StorageValidationDataSource(sharedPreference))
        controlRepository = ControlRepository(controlLocal)
        userRepository = UserRepository(userLocal)
    }


    private fun buildViewModel(): MainViewModel {
        val factory = MainViewModelFactory(validationRepository, controlRepository, userRepository)
        return ViewModelProvider(this, factory).get(MainViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        viewModel.doCloseOlderControls()
        checkHasControlToday()
        setDosisWidget()
    }

    fun checkHasControlToday() {
        if (Control.hasPendingControls()) {
            binding.btnINR.isEnabled = false
            binding.btnBorrar.isEnabled = true
            if (Control.hasControlToday() && User2.isAlarmTime()) {
                askForControl(Control.getControlDay(Date())?.recurso)
            } else flashBar?.dismiss()

        } else {
            binding.btnINR.isEnabled = true
            binding.btnBorrar.isEnabled = false
            binding.carousel.visibility = View.GONE
            binding.ivArrowLeft.visibility = View.GONE
            binding.ivArrowRight.visibility = View.GONE
            flashBar?.dismiss()
            MyWorkManager.clearAllWorks()
        }
    }


    fun askForControl(valor: String?) {

        if (flashBar == null || (flashBar != null && !flashBar!!.isShown())) {
            flashBar = Flashbar.Builder(this)
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
        Control2.updateCurrentControl(status)
    }

    fun setDosisWidget() {
        binding.carousel.visibility = View.VISIBLE
        val transformer = FlatMerryGoRoundTransformer()
        transformer.viewPerspective = 0.2
        transformer.farAlpha = 0.0
        binding.ivArrowLeft.visibility = View.VISIBLE
        binding.ivArrowRight.visibility = View.VISIBLE
        transformer.farScale = -1.5

        binding.carousel.transformer = transformer
        binding.carousel.isInfinite = true
        val items = ArrayList(Control2.getActiveControlList())
        binding.carousel.adapter = DoseAdapter(items)

        if (items != null && items.count() > 0) {
            val position =
                items.indexOf(items.filter { f -> f.fecha == Date().clearTime() }.first())
            binding.carousel.smoothScrollToPosition(position)
        }
    }

    private fun subscribeUI() {
        viewModel.statusINRButton.observe(this) { value ->
            binding.btnINR.isEnabled = value
        }
        viewModel.statusDeleteBtn.observe(this) { value ->
            binding.btnBorrar.isEnabled = value
        }
        viewModel.bloodValue.observe(this) { value ->
            binding.tvSangreValor.text = "${value.toString().replace(".", ",")}"
        }
        viewModel.doseValue.observe(this) { value ->
            binding.tvDosisValor.text = "${value}"
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
        val data = Control2.getAll()
        Log.e("MainActivity", "ANTES")
        for (item in data) {
        }
        Log.e("MainActivity", item.toString())

        Control2.restartIRN()
        viewModel.checkHasControlToday()

        Log.e("MainActivity", "DESPUES")
        for (item in Control2.getAll()) {
        }
        Log.e("MainActivity", item.toString())
    }

    fun doOpenEstadisticas() {
        startActivity(Intent(this, BarCharActivity::class.java))
    }

    fun doOpenMaps() {
        if (comprabarSiExisteApp("com.google.android.apps.maps", getApplicationContext())) {
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
        } else {
            Toast.makeText(this, "Se necesita tener instalado Google Maps", Toast.LENGTH_LONG)
                .show()
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

        val view: LinearLayout =
            layoutInflater.inflate(R.layout.ad_introducir_inr, null) as LinearLayout
        val editText = view.findViewById<EditText>(R.id.etValor)

        // Buscamos si hay historial..
        val ultimosControles = Control2.getUltimosIRN()

        if (ultimosControles.count() > 0) {
            val layoutHistorico = view.findViewById<LinearLayout>(R.id.ll_historico)
            layoutHistorico.visibility = View.VISIBLE
            val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroup)

            for (x in 0 until ultimosControles.count()) {
                val chip = getLayoutInflater().inflate(
                    R.layout.layout_chip_choice,
                    chipGroup,
                    false
                ) as Chip
                chip.text = ultimosControles[x]
                chipGroup.addView(chip)

                chip.setOnClickListener {
                    editText.setText(chip.text)
                }
            }
        }

        editText.addTextChangedListener(object : TextWatcher {
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
                alert(message = "Consulte los datos con su MÉDICO")
                return@OnClickListener
            }
            val dataNiveles = AppContext.getNivelFromFichero(
                nomFichero,
                nivelyDias["nivel"].toString(),
                nivelyDias["dias"].toString()
            )

            Timer("SettingUp", false).schedule(500) {
                runOnUiThread {
                    doAskPlanificacion(
                        sangre = "${AppContext.validarInputTextSangre(editText.text.toString())}",
                        nivel = nivelyDias["nivel"].toString(),
                        dataNiveles = dataNiveles
                    )
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

        if (!Control2.any()) {
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

        builder.setPositiveButton(
            "Planificar",
            DialogInterface.OnClickListener { dialogInterface, i ->
                // Actualizamos la sangre y nivel
                MySharedPreferences.shared.addString("sangre", sangre)
                MySharedPreferences.shared.addString("nivel", nivel)
                Control2.registrarControlActual(dataNiveles, sangre.toFloat(), nivel.toInt())
                viewModel.checkHasControlToday()

                MyWorkManager.setWorkers(Control2.getActiveControlList(onlyPendings = true))

                for (item in Control2.getAll()) {
                }
                Log.e("MainActivity", item.toString())


                if (btnMails.isChecked) {
                }
                sendEmailPlanificacion()

            })

        builder.create()
        builder.show()
    }

    fun sendEmailPlanificacion() {
        val data = Html.fromHtml(Control2.getActiveControlListToEmail())
        val emailIntent = Intent(
            Intent.ACTION_SENDTO,
            Uri.fromParts("mailto", MySharedPreferences.shared.getString("emails"), null)
        )
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Planificacion IRN")
        emailIntent.putExtra(Intent.EXTRA_TEXT, data)
        startActivity(Intent.createChooser(emailIntent, "Enviar mail..."))
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

    fun comprabarSiExisteApp(nombrePaquete: String, context: Context): Boolean {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo(nombrePaquete, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun doCalendario() {
        startActivity(Intent(this, CalendarioActivity::class.java))
    }
}
