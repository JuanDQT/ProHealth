package com.juan.prohealth.ui.mainActiviy

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.Context
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
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.repository.UserRepository
import com.juan.prohealth.repository.ValidationRepository
import com.juan.prohealth.ui.adapters.DoseAdapter
import com.juan.prohealth.ui.ajustesActivity.AjustesActivity
import com.juan.prohealth.ui.common.*
import kotlinx.android.synthetic.main.custom_button.view.*
import java.util.*
import com.juan.prohealth.database.Control as RealmControl


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var validationRepository: ValidationRepository
    private lateinit var controlRepository: ControlRepository
    private lateinit var userRepository: UserRepository
    private lateinit var adapter: DoseAdapter

    private val flashBar: Flashbar by lazy { instanceFlashBar() }
    private lateinit var userResourceImage: String
    private val inrAlertDialog: AlertDialog by lazy { instanceINRAlertDialog() }

    private var lastBloodValues = emptyArray<Float>()
    private var currentBloodValue = 0f

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

    // TODO: Para solucionar el error visual de las imagenes en el carousel que se van ocultando, podemos hacer 2 cosas:
    // 1. Añadir mas decimales al transformer.viewPerspective. Cuanto mas pequeño el valor, mas retrasaremos el error. con 0.01 es una buena partida, pero añadimos mas para asegurar
    // 2. Solucion definitiva: no utilizar el transformer en el carousel. Esto implicaria que se perderia el efecto visual
    // El error ocurre, por que parece que al añadirle una perspectiva(obligatoria) esta se queda guardada en memoria, y cada vez va aumentando
    // La perspectiva consiste en añadirle una especie de margen superior, a mas grande, mas va desplazando el widget hacia abajo.
    private fun configureWidget() {
        binding.carousel.visibility = View.VISIBLE
        val transformer = FlatMerryGoRoundTransformer()
        transformer.viewPerspective = 1.0E-45
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
        builder.setTitle(R.string.title_introducir)
        val view: LinearLayout =
            layoutInflater.inflate(R.layout.ad_introducir_inr, null) as LinearLayout

        val initialLayout = view.findViewById<LinearLayout>(R.id.frame_initial)
        val secondLayout = view.findViewById<LinearLayout>(R.id.frame_final)

        val editText = view.findViewById<EditText>(R.id.etValor)
        val btnAccept = view.findViewById<Button>(R.id.btn_accept)

        // Buscamos si hay historial..

        if (lastBloodValues.count() > 0) {
            val layoutHistorico = view.findViewById<LinearLayout>(R.id.ll_historico)
            layoutHistorico.visibility = View.VISIBLE
            val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroup)

            for (x in 0 until lastBloodValues.count()) {
                val chip = getLayoutInflater().inflate(
                    R.layout.layout_chip_choice,
                    chipGroup,
                    false
                ) as Chip
                chip.text = "${lastBloodValues[x]}"
                chipGroup.addView(chip)

                chip.setOnClickListener {
                    editText.setText(chip.text)
                }
            }
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                AppContext.validarInputTextSangre(editText.text.toString())?.let {
                    btnAccept.isEnabled = true
                    return
                }
                btnAccept.isEnabled = false
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        btnAccept.setOnClickListener {
            editText.hideKeyboard()
            val valorSangreNumerico = editText.text.toString().replace(",", ".").toFloat()
            val nomFichero = Control.getFicheroCorrespondiente(valorSangreNumerico)

            val nivelyDias: Map<String, Int> = Control.getNivelCorrespondiente(valorSangreNumerico, viewModel.doseValue.value!!)

            if (nivelyDias["nivel"]!! < 1) {
                alert(message = "Consulte los datos con su MÉDICO")
            }
            val dataNiveles = AppContext.getNivelFromFichero(
                nomFichero,
                nivelyDias["nivel"].toString(),
                nivelyDias["dias"].toString()
            )

            // levantamos segunda vista
            btnAccept.text = getString(R.string.button_planificar)
            initialLayout.visibility = View.GONE
            secondLayout.visibility = View.VISIBLE
            val irnActual = view.findViewById<TextView>(R.id.tvIRN)
            val irnNew = view.findViewById<TextView>(R.id.tvIRNNew)
            val btnMails = view.findViewById<CheckBox>(R.id.cb_mails)

            irnActual.text = "${currentBloodValue}"
            irnNew.text = "${AppContext.validarInputTextSangre(editText.text.toString())}"

            val irnText = view.findViewById<TextView>(R.id.tvIRN_text)
            val frameIRN = view.findViewById<FrameLayout>(R.id.frame_IRN)
            if (currentBloodValue == 0f) {
                irnText.visibility = View.GONE
                frameIRN.visibility = View.GONE
            } else {
                irnText.visibility = View.VISIBLE
                frameIRN.visibility = View.VISIBLE
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
            val btnFinish = view.findViewById<Button>(R.id.btn_finish)

            btnFinish.setOnClickListener {
                viewModel.updateUserData(
                    irnNew.text.toString().toFloat(),
                    nivelyDias["nivel"].toString().toInt(),
                    dataNiveles.toTypedArray(),
                    Control()
                )
                if (btnMails.isChecked) {
                    sendPlanningEmail()
                }
                inrAlertDialog.cancel()
            }
        }

        builder.setView(view)

        val ad = builder.create()

        ad.setOnCancelListener {
            initialLayout.visibility = View.VISIBLE
            secondLayout.visibility = View.GONE
            editText.text = null
        }
        return ad
    }

    private fun reorderButtonList() {
        val allButtons = arrayListOf<CustomButton>()

        for (i in 0 until binding.llOpciones.childCount) {
            val button = binding.llOpciones.getChildAt(i)

            if (button is CustomButton && button.visibility == View.VISIBLE)
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

        viewModel.chipsBloodValues.observe(this) { values ->
            lastBloodValues = values
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
                val position =
                    activeControls.indexOf(activeControls.filter { f -> f.executionDate == Date().clearTime() }
                        .first())
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

    private fun navigateToStatsGraphic() {
        startActivity(Intent(this, GraphActivity::class.java))
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
