package com.juan.prohealth.ui.mainActiviy

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.andrognito.flashbar.Flashbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.gtomato.android.ui.transformer.FlatMerryGoRoundTransformer
import com.juan.prohealth.*
import com.juan.prohealth.custom.CustomButton
import com.juan.prohealth.data.local.SharedPreference
import com.juan.prohealth.data.local.StorageValidationDataSource
import com.juan.prohealth.database.room.Control
import com.juan.prohealth.database.room.MyDatabase
import com.juan.prohealth.database.room.RoomControlDataSource
import com.juan.prohealth.database.room.RoomUserDataSource
import com.juan.prohealth.databinding.ActivityMainBinding
import com.juan.prohealth.databinding.CustomButtonBinding
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.repository.UserRepository
import com.juan.prohealth.repository.ValidationRepository
import com.juan.prohealth.ui.adapters.DoseAdapter
import com.juan.prohealth.ui.ajustesActivity.AjustesActivity
import com.juan.prohealth.ui.common.*
import kotlinx.android.synthetic.main.ad_planificacion.view.*
import kotlinx.android.synthetic.main.custom_button.view.*
import java.util.*
import kotlin.collections.set
import kotlin.concurrent.schedule
import com.juan.prohealth.database.Control as RealmControl


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var validationRepository: ValidationRepository
    private lateinit var controlRepository: ControlRepository
    private lateinit var userRepository: UserRepository
    private lateinit var adapter: DoseAdapter
    private val RANGO_AZUL: String = "rangoBajoAzul.json"
    private val RANGO_ROJO: String = "rangoAltoRojo.json"

    private val flashBar: Flashbar by lazy { instanceFlashBar() }
    private lateinit var userResourceImage: String
    private val inrAlertDialog: AlertDialog by lazy { instanceINRAlertDialog() }
    private val planificationAlertDialog: AlertDialog by lazy { instancePlanningAlertDialog() }

    private var bloodLastValues = emptyArray<Float>()
    private var currentBloodValue = 0f
    private var sangreString = ""
    private var nivelString = ""
    private var dataNiveles = emptyArray<String>()

    private val locationPermission = PermissionRequester(this, ACCESS_COARSE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildDependencies()
        viewModel = buildViewModel()
        setUpUi()
        subscribeUI()
    }

    private fun setUpUi() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = DoseAdapter(emptyList())
        binding.btnINR.setOnClickListener(this)
        binding.btnGmap.setOnClickListener(this)
        binding.btnEstadisticas.setOnClickListener(this)
        binding.btnCalendario.setOnClickListener(this)
        binding.btnAjustes.setOnClickListener(this)
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
            .negativeActionText("Hoy no tomaré")
            .positiveActionTextColorRes(R.color.colorAccent)
            .negativeActionTextColorRes(R.color.colorAccent)
            .positiveActionTapListener(object : Flashbar.OnActionTapListener {
                override fun onActionTapped(bar: Flashbar) {
                    viewModel.updateCurrentControlStatus(1)
                    bar.dismiss()
                }
            })
            .negativeActionTapListener(object : Flashbar.OnActionTapListener {
                override fun onActionTapped(bar: Flashbar) {
                    viewModel.updateCurrentControlStatus(0)
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

        if (bloodLastValues.count() > 0) {
            val layoutHistorico = view.findViewById<LinearLayout>(R.id.ll_historico)
            layoutHistorico.visibility = View.VISIBLE
            val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroup)

            for (x in 0 until bloodLastValues.count()) {
                val chip = getLayoutInflater().inflate(
                    R.layout.layout_chip_choice,
                    chipGroup,
                    false
                ) as Chip
                chip.text = "${bloodLastValues[x]}"
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
                    this@MainActivity.dataNiveles = dataNiveles.toTypedArray()
                    planificationAlertDialog.show()
                }
            }
        })

        val ad = builder.create()
        positiveButton = ad.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton?.isEnabled = false
        return ad
    }

    private fun reorderButtonList() {
        val allButtons = arrayListOf<CustomButton>()

        for (i in 0 until binding.llOpciones.childCount) {
            val button = binding.llOpciones.getChildAt(i)

            if(button is CustomButton && button.visibility == View.VISIBLE)
                allButtons.add(button)
        }

        for (i in 0 until allButtons.size) {
            val button = allButtons[i]

            if (i % 2 == 0) {
                button.containIconRight.visibility = View.GONE
                button.containIcon.visibility = View.VISIBLE
            } else {
                button.containIcon.visibility = View.GONE
                button.containIconRight.visibility = View.VISIBLE
            }
            button.menuText.textAlignment = View.TEXT_ALIGNMENT_CENTER
        }
    }

    private fun buildViewModel(): MainViewModel {
        val factory = MainViewModelFactory(validationRepository, controlRepository, userRepository)
        return ViewModelProvider(this, factory).get(MainViewModel::class.java)
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

    private fun subscribeUI() {

        viewModel.bloodValue.observe(this) { value ->
            currentBloodValue = value
            binding.tvSangreValor.text = "${value.toString().replace(".", ",")}"
        }
        viewModel.doseValue.observe(this) { value ->
            binding.tvDosisValor.text = "${value}"
        }

        viewModel.userResourceImage.observe(this) { resourceValue ->
            userResourceImage = resourceValue
        }

        viewModel.checkPendingControls.observe(this, { isPendingControls ->
            if (isPendingControls) flashBar.show()
            else Log.i("IsPendingControls", "Doesn't have pending controls")
        })
        viewModel.currentActiveControls.observe(this, { activeControls ->
            if (activeControls.isNullOrEmpty()) {
                Log.i("ActiveControls", "Hide Widget")
                binding.carousel.visibility = View.GONE
                binding.ivArrowLeft.visibility = View.GONE
                binding.ivArrowRight.visibility = View.GONE
                binding.btnINR.visibility = View.VISIBLE

            } else {
                binding.btnINR.visibility = View.GONE
                binding.ivArrowLeft.visibility = View.VISIBLE
                binding.ivArrowRight.visibility = View.VISIBLE
                binding.carousel.visibility = View.VISIBLE
                adapter.setItems(activeControls)
                binding.carousel.adapter = adapter
                val position = activeControls.indexOf(activeControls.filter { f -> f.executionDate == Date().clearTime() }.first())
                binding.carousel.smoothScrollToPosition(position)
                Log.i("ActiveControls", "Show Widget")
            }
            reorderButtonList()

        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.getControlsToFillCarousel()
        viewModel.checkHasControlToday()
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                R.id.btnINR -> inrAlertDialog.show()
                R.id.btnGmap -> checkPermissionLocation()
                R.id.btnEstadisticas -> navigateToStatsGraphic()
                R.id.btnCalendario -> navigateToCalendar()
                R.id.btnAjustes -> startActivity(Intent(this, AjustesActivity::class.java))
            }
        }
    }

    private fun clickOnDeleteLastINRGroup() {
        viewModel.deleteLastControlGroup()
    }

    private fun navigateToStatsGraphic() {
        startActivity(Intent(this, BarCharActivity::class.java))
    }

    private fun showNearPharmaciesWithGmapsApp() {

        if (checkIfExistGmapsApp("com.google.android.apps.maps", getApplicationContext())) {
            val gmmIntentUri = Uri.parse("geo:0,0?q=farmacia+de+guardia")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            mapIntent.resolveActivity(packageManager)?.let {
                startActivity(mapIntent)
            }
        } else {
            toast("Do not have install Google Maps application")
        }
    }

    /**
     * Cuando introducimos una NUEVA lectura de Sangre aqui recalculamos los dias de control ( 4 o 7 )
     * validamos el campo nivel de sangre que recibimos y aplicamos los calculos
     * para seleccionar los valores de salida e imprimirlos
     */

    // antes: doAskPlanificacion
    private fun instancePlanningAlertDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Informacion")
        //val planingBinding = AdPlanificacionBinding.inflate(LayoutInflater.from(this))
        val view = layoutInflater.inflate(R.layout.ad_planificacion, null)
        val currentINR = view.findViewById<TextView>(R.id.tvIRN)
        val newINR = view.findViewById<TextView>(R.id.tvIRNNew)
        val btnMails = view.findViewById<CheckBox>(R.id.cb_mails)

        currentINR.text = "${currentBloodValue}"
        newINR.text = sangreString

        if (currentBloodValue == 0f) {
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
            DialogInterface.OnClickListener { _, _ ->
                // Actualizamos la sangre y nivel
                viewModel.updateUserData(
                    sangreString.toFloat(),
                    nivelString.toInt(),
                    dataNiveles,
                    Control()
                )
                // TODO: Recoge los valroes para mostrarlo en un ALERT. Implemendado guardado en variable observable y recogido
                if (btnMails.isChecked) {
                    sendPlanningEmail()
                }
            })

        return builder.create()
    }

    // TODO: Recoge los valores para mostrarlo en un ALERT
    private fun sendPlanningEmail() {
        val data = Html.fromHtml(RealmControl.getActiveControlListToEmail())
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
    private fun getFicheroCorrespondiente(valor: Float): String {
        if (valor >= 1.0 && valor <= 3.6) {
            return RANGO_AZUL
        } else return RANGO_ROJO
    }

    /**
     * En funcion del nivel de sangre como parametro recibidoharemos una operacion de suma, resta o simplemente nada
     * segun el rango al que pertenezca este numero float devolvemos un Map[nivel,dias] Modificado - Aqui solo se vera afectada
     * el campo nivel ( suma - resta o dejado igual)
     */
    private fun getNivelCorrespondiente(valor: Float): MutableMap<String, Int> {
        val map = mutableMapOf<String, Int>()
        when (valor) {
            // Azules
            in 1.0..1.5 -> {
                map["nivel"] = (getCurrentDoseLevel().toInt() + 2)
                map["dias"] = 3
            }
            in 1.6..2.3 -> {
                map["nivel"] = (getCurrentDoseLevel().toInt() + 1)
                map["dias"] = 4
            }
            in 2.4..3.6 -> {
                map["nivel"] = (getCurrentDoseLevel().toInt())
                map["dias"] = 7
            }
            // Rojos
            in 3.7..4.9 -> {
                map["nivel"] = (getCurrentDoseLevel().toInt() - 1)
                map["dias"] = 7
            }
            in 5.0..7.0 -> {
                map["nivel"] = (getCurrentDoseLevel().toInt() - 2)
                map["dias"] = 4
            }
        }

        return map
    }

    /**
     * Obtenemos el valor del JSON que hemos
     * guardado en SharedPreferences
     */
    private fun getCurrentDoseLevel(): String {
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

    private fun checkIfExistGmapsApp(namePackage: String, context: Context): Boolean {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo(namePackage, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun navigateToCalendar() {
        startActivity(Intent(this, CalendarioActivity::class.java))
    }

    private fun checkPermissionLocation() {
        locationPermission.request { isGranted ->
            if (isGranted) showNearPharmaciesWithGmapsApp() else showAlertMessageUi()
        }
    }

    private fun showAlertMessageUi() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission is required")
            .setMessage("This permission is required to know your actual position and to can looking for near pharmacies")
            .setPositiveButton("Allow") { dialog, _ ->
                dialog.cancel()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Deny") { dialog, _ -> dialog.cancel() }
            .show()
    }
}
