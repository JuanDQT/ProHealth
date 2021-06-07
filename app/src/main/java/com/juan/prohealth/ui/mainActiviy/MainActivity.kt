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
import com.juan.prohealth.database.MyDatabase
import com.juan.prohealth.databinding.ActivityMainBinding
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.repository.UserRepository
import com.juan.prohealth.repository.ValidationRepository
import com.juan.prohealth.ui.adapters.DoseAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.set
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var validationRepository: ValidationRepository
    private lateinit var controlRepository: ControlRepository
    private lateinit var userRepository: UserRepository
    private val RANGO_AZUL: String = "rangoBajoAzul.json"
    private val RANGO_ROJO: String = "rangoAltoRojo.json"
    private val flashBar: Flashbar by lazy { instanceFlashBar() }
    private val inrAlertDialog: AlertDialog by lazy { instanceINRAlertDialog() }
    private val planificationAlertDialog: AlertDialog by lazy { instancePlanificationAlertDialog() }
    private lateinit var userResourceImage: String
    private var bloodLastValues = emptyArray<Float>()
    private var currentBloodValue = 0f
    private lateinit var adapter:DoseAdapter

    private var sangreString = ""
    private var nivelString = ""
    private var dataNieveles = emptyArray<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        buildDependencies()
        viewModel = buildViewModel()
        setContentView(binding.root)
        setUpUi()
        subscribeUI()
    }

    private fun setUpUi() {
        adapter = DoseAdapter(emptyList())
        binding.carousel.adapter = adapter
        binding.btnBorrar.setOnClickListener(this)
        binding.btnINR.setOnClickListener(this)
        binding.btnGmap.setOnClickListener(this)
        binding.btnEstadisticas.setOnClickListener(this)
        binding.btnCalendario.setOnClickListener(this)
        binding.btnAjustes.setOnClickListener(this)
        instanceFlashBar()
        instanceINRAlertDialog()
        configureWidget()
    }

    private fun configureWidget() {
        binding.carousel.visibility = View.VISIBLE
        val transformer = FlatMerryGoRoundTransformer()
        transformer.viewPerspective = 0.2
        transformer.farAlpha = 0.0
        binding.ivArrowLeft.visibility = View.VISIBLE
        binding.ivArrowRight.visibility = View.VISIBLE
        transformer.farScale = -1.5

        binding.carousel.transformer = transformer
        binding.carousel.isInfinite = true
    }

    private fun instanceFlashBar(): Flashbar {
        return Flashbar.Builder(this)
            .gravity(Flashbar.Gravity.BOTTOM)
            .title(getString(R.string.title_notificacion))
            .message(String.format(getString(R.string.msg_notificacion, userResourceImage)))
            .enableSwipeToDismiss()
            .backgroundColorRes(R.color.colorPrimaryDark)
            .positiveActionText("Si")
            .negativeActionText("Hoy no tomare")
            .positiveActionTextColorRes(R.color.colorAccent)
            .negativeActionTextColorRes(R.color.colorAccent)
            .positiveActionTapListener(object : Flashbar.OnActionTapListener {
                override fun onActionTapped(bar: Flashbar) {
                    viewModel.updateCurrentControlStatus(true)
                    bar.dismiss()
                }
            })
            .negativeActionTapListener(object : Flashbar.OnActionTapListener {
                override fun onActionTapped(bar: Flashbar) {
                    viewModel.updateCurrentControlStatus(false)
                    bar.dismiss()
                }
            })
            .build()
    }

    private fun instanceINRAlertDialog(): AlertDialog {
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
                    sangreString = "${AppContext.validarInputTextSangre(editText.text.toString())}"
                    nivelString = nivelyDias["nivel"].toString()
                    dataNieveles = dataNiveles.toTypedArray()
                    planificationAlertDialog.show()
                }
            }
        })

        val ad = builder.create()
        positiveButton = ad.getButton(AlertDialog.BUTTON_POSITIVE) as Button?
        positiveButton?.isEnabled = false
        return ad
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
        viewModel.checkHasControlToday()
    }

    private fun subscribeUI() {
        viewModel.statusINRButton.observe(this) { value ->
            binding.btnINR.isEnabled = value
        }
        viewModel.statusDeleteBtn.observe(this) { value ->
            binding.btnBorrar.isEnabled = value
        }
        viewModel.bloodValue.observe(this) { value ->
            currentBloodValue = value
            binding.tvSangreValor.text = "${value.toString().replace(".", ",")}"
        }
        viewModel.doseValue.observe(this) { value ->
            binding.tvDosisValor.text = "${value}"
        }
        viewModel.showAlertControl.observe(this) { value ->
            if (value) {
            }
            flashBar.show()
        }
        viewModel.dismissFlashBar.observe(this) { value ->
            flashBar?.let {
                if (value) {
                }
                it.dismiss()
            }
        }
        viewModel.visibilityGroupCarousel.observe(this) { value ->
            binding.carousel.visibility = value
            binding.ivArrowLeft.visibility = value
            binding.ivArrowRight.visibility = value
        }

        viewModel.userResourceImage.observe(this) {
            userResourceImage = it
        }

        viewModel.lastBloodValues.observe(this) { array ->
            bloodLastValues = array
        }

        viewModel.controls.observe(this,{activeControls ->
            adapter.setItems(activeControls)
        })
    }


    // Controlamos eventos click Botones
    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                R.id.btnBorrar -> doDeleteLastINRGroup()
                R.id.btnINR -> inrAlertDialog.show()
                R.id.btnGmap -> doOpenMaps()
                R.id.btnEstadisticas -> startActivity(Intent(this, BarCharActivity::class.java))
                R.id.btnCalendario -> startActivity(Intent(this, CalendarioActivity::class.java))
                R.id.btnAjustes -> startActivity(Intent(this, AjustesActivity::class.java))
            }
        }
    }

    fun doDeleteLastINRGroup() {
        viewModel.deleteLastControlGroup()
        viewModel.checkHasControlToday()
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

    // antes: doAskPlanificacion
    fun instancePlanificationAlertDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Informacion")

        val view = layoutInflater.inflate(R.layout.ad_planificacion, null)
        val irnActual = view.findViewById<TextView>(R.id.tvIRN)
        val irnNew = view.findViewById<TextView>(R.id.tvIRNNew)
        val btnMails = view.findViewById<CheckBox>(R.id.cb_mails)

        irnActual.text = "${currentBloodValue}"
        irnNew.text = sangreString

        // TODO: revisar
        if (currentBloodValue == 0f) {
            val irnText = view.findViewById<TextView>(R.id.tvIRN_text)
            val frameIRN = view.findViewById<FrameLayout>(R.id.frame_IRN)
            irnText.visibility = View.GONE
            frameIRN.visibility = View.GONE
        }


        for (x in 0 until 7) {
            val layout = view.findViewWithTag<LinearLayout>("l${x}")

            if (x >= dataNieveles.size) {
                layout.visibility = View.GONE
                continue
            }

            layout.visibility = View.VISIBLE

            // sobrescribimos valor
            val textView = view.findViewWithTag<TextView>("t${x}")
            textView.text = if (dataNieveles[x].isNullOrEmpty()) "No toca" else dataNieveles[x]

            // sobreescribimos imagen
            if (!dataNieveles[x].isNullOrEmpty()) {
                val imageView = view.findViewWithTag<ImageView>("i${x}")
                imageView.setBackgroundResource(AppContext.getImageNameByJSON(dataNieveles[x]))
            }

        }
        builder.setView(view)

        builder.setPositiveButton(
            "Planificar",
            DialogInterface.OnClickListener { dialogInterface, i ->
                // Actualizamos la sangre y nivel
                viewModel.updateUserData(sangreString.toFloat(), nivelString.toInt())
                viewModel.insertNewControls(dataNieveles, sangreString.toFloat(), nivelString.toInt())
                viewModel.checkHasControlToday()

                MyWorkManager.setWorkers(Control2.getActiveControlList(onlyPendings = true))

                if (btnMails.isChecked) {
                }
                sendEmailPlanificacion()

            })

        return builder.create()
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
}
