package com.juan.prohealth.ui.mainActiviy

import android.app.ProgressDialog
import android.content.DialogInterface
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.juan.prohealth.*
import com.juan.prohealth.database.Control2
import com.juan.prohealth.database.User2
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.repository.UserRepository
import com.juan.prohealth.repository.ValidationRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(
    private val validationRepository: ValidationRepository,
    private val controlRepository: ControlRepository,
    private val userRepository: UserRepository
) :
    ViewModel() {

    private var _statusINRButton = MutableLiveData(true)
    val statusINRButton: LiveData<Boolean> get() = _statusINRButton

    private var _statusDeleteBtn = MutableLiveData(false)
    val statusDeleteBtn: LiveData<Boolean> get() = _statusDeleteBtn

    private var _bloodValue = MutableLiveData(0f)
    val bloodValue: LiveData<Float> get() = _bloodValue

    private var _doseValue = MutableLiveData(0)
    val doseValue: LiveData<Int> get() = _doseValue

    // New
    fun doCloseOlderControls() {
        viewModelScope.launch {
            controlRepository.closeOldControls()
        }
    }

    // metodos

    fun checkHasControlToday() {
        viewModelScope.launch {
            if (controlRepository.hasPendingControls()) {
                _statusINRButton.value = false
                _statusDeleteBtn.value = true
//                setDosisWidget()
                if (controlRepository.hasPendingControlToday() && User2.isAlarmTime())
                    askForControl(Control2.getControlDay(Date())?.recurso)
                else flashBar?.dismiss()

            } else {
                _statusINRButton.value = true
                _statusDeleteBtn.value = false
                binding.carousel.visibility = View.GONE
                binding.ivArrowLeft.visibility = View.GONE
                binding.ivArrowRight.visibility = View.GONE
                flashBar?.dismiss()
                MyWorkManager.clearAllWorks()
            }
        }

    }

    fun doOnResume() {

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

                                            val fechaFin =
                                                SimpleDateFormat("yyyy-MM-dd").parse(it.getString("fechaFin"))
                                            MySharedPreferences.shared.setFechaFinPrueba(fechaFin.clearTime().time)

                                            onResume()
                                        }
                                    }
                                }

                                override fun onError(anError: ANError?) {
                                    pdLoading.dismiss()
                                    alert(
                                        getString(R.string.alerta),
                                        getString(R.string.error_verificacion)
                                    )
                                }
                            })

                        }, closable = false
                    )
                }
            } else {
                alert(
                    "Alerta",
                    "Por favor, no juege con las fechas. Vuelva a situarla a ${Date(currentDate).customFormat()}",
                    "Aceptar",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        finishAffinity()
                    })
            }
        }

        Control2.closeOlderControls()

        pintarValores()
        checkHasControlToday()
    }

}