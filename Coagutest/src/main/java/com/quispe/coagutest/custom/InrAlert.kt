package com.quispe.coagutest.custom

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.quispe.coagutest.AppContext
import com.quispe.coagutest.ControlManager
import com.quispe.coagutest.R
import com.quispe.coagutest.database.room.Control
import com.quispe.coagutest.ui.common.alert
import com.quispe.coagutest.ui.common.hideKeyboard
import com.quispe.coagutest.ui.common.setBackgroundResource

interface InrAlertImp {
    fun doCreatePlanning(
        bloodValue: Float,
        doseLevel: Int,
        planning: Array<String>,
        sendWithEmail: Boolean)

    fun errorPlanning()
}
class InrAlert(activity: Activity, layoutInflater: LayoutInflater) {
    private val LAYOUT_ID = R.layout.ad_introducir_inr
    private val layoutInflater: LayoutInflater
    var initialDose: Int = 0
    var currentBloodValue: Float = 0f

    private val initialLayoutView by lazy { rootView.findViewById<LinearLayout>(R.id.frame_initial) }
    private val secondaryLayoutView by lazy { rootView.findViewById<LinearLayout>(R.id.frame_final) }

    private val inputInr: EditText by lazy { rootView.findViewById<EditText>(R.id.etValor) }


    var latestBloodValue: Array<Float> = arrayOf()
    set(value) {
        field = value
        drawChips()
    }
    val rootView by lazy { layoutInflater.inflate(LAYOUT_ID, null) as LinearLayout }

    private var alertDialog: AlertDialog
    var alertDialogListener: InrAlertImp? = null

    fun drawChips() {
        if (latestBloodValue.count() > 0) {
            val layoutHistorico = rootView.findViewById<LinearLayout>(R.id.ll_historico)
            layoutHistorico.visibility = View.VISIBLE
            val chipGroup = rootView.findViewById<ChipGroup>(R.id.chipGroup)

            for (x in 0 until latestBloodValue.count()) {
                val chip = layoutInflater.inflate(
                    R.layout.layout_chip_choice,
                    chipGroup,
                    false
                ) as Chip
                chip.text = "${latestBloodValue[x]}"
                chipGroup.addView(chip)

                chip.setOnClickListener {
                    inputInr.setText(chip.text)
                }
            }
        }

    }

    fun showAlert() {
        alertDialog.show()
    }

    fun dismissAlert() {
        alertDialog.dismiss()
    }

    init {
        this.layoutInflater = layoutInflater
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.title_introducir)

        val btnAccept = rootView.findViewById<Button>(R.id.btn_accept)

        inputInr.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                AppContext.validarInputTextSangre(inputInr.text.toString())?.let {
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
            inputInr.hideKeyboard()
            val valorSangreNumerico = inputInr.text.toString().replace(",", ".").toFloat()
            val nomFichero = ControlManager.getFicheroCorrespondiente(valorSangreNumerico)

            val nivelyDias: Map<String, Int> = ControlManager.getNivelCorrespondiente(valorSangreNumerico, initialDose)

            if (nivelyDias["nivel"]!! < 1) {
                //alert(message = "Consulte los datos con su MÉDICO")
                alertDialogListener?.errorPlanning()
            }
            val dataNiveles = AppContext.getNivelFromFichero(
                nomFichero,
                nivelyDias["nivel"].toString(),
                nivelyDias["dias"].toString()
            )

            // TODO: continue optimizate:
            // 1. Clear
            // 2. Prepare listeners
            // 3. Delegate controls to class ControlManager
            // 4. Connect with viewModel
            // levantamos segunda vista
            btnAccept.text = activity.getString(R.string.button_planificar)
            initialLayoutView.visibility = View.GONE
            secondaryLayoutView.visibility = View.VISIBLE
            val irnActual = rootView.findViewById<TextView>(R.id.tvIRN)
            val irnNew = rootView.findViewById<TextView>(R.id.tvIRNNew)
            val btnMails = rootView.findViewById<CheckBox>(R.id.cb_mails)

            irnActual.text = "${currentBloodValue}"
            irnNew.text = "${AppContext.validarInputTextSangre(inputInr.text.toString())}"

            val irnText = rootView.findViewById<TextView>(R.id.tvIRN_text)
            val frameIRN = rootView.findViewById<FrameLayout>(R.id.frame_IRN)
            if (currentBloodValue == 0f) {
                irnText.visibility = View.GONE
                frameIRN.visibility = View.GONE
            } else {
                irnText.visibility = View.VISIBLE
                frameIRN.visibility = View.VISIBLE
            }


            for (x in 0 until 7) {
                val layout = rootView.findViewWithTag<LinearLayout>("l${x}")

                if (x >= dataNiveles.size) {
                    layout.visibility = View.GONE
                    continue
                }

                layout.visibility = View.VISIBLE

                // sobrescribimos valor
                val textView = rootView.findViewWithTag<TextView>("t${x}")
                textView.text = if (dataNiveles[x].isNullOrEmpty()) "No toca" else dataNiveles[x]

                // sobreescribimos imagen
                if (!dataNiveles[x].isNullOrEmpty()) {
                    val imageView = rootView.findViewWithTag<ImageView>("i${x}")
                    imageView.setBackgroundResource(AppContext.getImageNameByJSON(dataNiveles[x]))
                }
            }
            val btnFinish = rootView.findViewById<Button>(R.id.btn_finish)

            btnFinish.setOnClickListener {
                alertDialogListener?.doCreatePlanning(irnNew.text.toString().toFloat(),
                    nivelyDias["nivel"].toString().toInt(),
                    dataNiveles.toTypedArray(),
                    btnMails.isChecked)
            }
        }

        builder.setView(rootView)

        alertDialog = builder.create()

        alertDialog.setOnCancelListener {
            initialLayoutView.visibility = View.VISIBLE
            secondaryLayoutView.visibility = View.GONE
            inputInr.text = null
        }
    }



}