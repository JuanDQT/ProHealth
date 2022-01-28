package com.quispe.coagutest.ui.ajustesActivity

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quispe.coagutest.AppContext
import com.quispe.coagutest.MyWorkManager
import com.quispe.coagutest.database.room.Control
import com.quispe.coagutest.repository.ControlRepository
import com.quispe.coagutest.repository.UserRepository
import com.quispe.coagutest.ui.common.customFormat
import io.github.lucasfsc.html2pdf.Html2Pdf
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class AjustesViewModel(
    private val controlRepository: ControlRepository,
    private val userRepository: UserRepository
): ViewModel() {

    private var mUserAlarm = MutableLiveData<Pair<Int, Int>>()
    private var mUserAlarmForAlert = MutableLiveData<Pair<Int, Int>>()

    private var _currentActiveControls = MutableLiveData<List<Control>>()
    val currentActiveControls: LiveData<List<Control>> get() = _currentActiveControls

    private var mUserEmailInfo = MutableLiveData<String?>()
    private var mExportControls = MutableLiveData<String?>()

    private var hourUser: Int? = 0
    private var minuteUser: Int? = 0

    init {
        getCurrentGroupControl()
    }

    fun deleteLastGroupControl() {
        viewModelScope.launch {
            Log.i("BTN REBOOT INR","Se procede a borrar el ultimo grupo de control")
            _currentActiveControls.value = emptyList()
            controlRepository.deleteLastControlGroup()
            updateUser()
        }
    }

    private fun updateUser() {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()

            val lastControlUser = controlRepository.getLastControl()
            lastControlUser?.let {
                currentUser.blood = it.blood
                currentUser.level = it.doseLevel
            }?: run {
                currentUser.level = currentUser.initialLevel
                currentUser.blood = 0f
            }

            userRepository.updateUser(currentUser)
        }
    }

    fun getUserEmailInfo(): MutableLiveData<String?> {
        return mUserEmailInfo
    }

    fun getExportControls(): MutableLiveData<String?> {
        return mExportControls
    }

    fun loadUserEmailInfo() {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            mUserEmailInfo.value = currentUser.trustEmails
        }
    }

    fun loadUserAlarm() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            hourUser = user.hourAlarm
            minuteUser = user.minuteAlarm
            mUserAlarm.value = Pair(user.hourAlarm, user.minuteAlarm)
        }
    }

    fun loadUserAlarmForAlert() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            hourUser = user.hourAlarm
            minuteUser = user.minuteAlarm
            mUserAlarmForAlert.value = Pair(user.hourAlarm, user.minuteAlarm)
        }
    }

    fun getUserAlarm(): MutableLiveData<Pair<Int, Int>> {
        return mUserAlarm
    }

    fun getUserAlarmForAlert(): MutableLiveData<Pair<Int, Int>> {
        return mUserAlarmForAlert
    }

    fun updateUserSchedule(hour: Int, minute: Int) {
        viewModelScope.launch {
            userRepository.updateUserSchedule(hour, minute)
            val pendingControls = controlRepository.getAllPendingControls()
            MyWorkManager.setWorkers(pendingControls, hour, minute)
        }
    }

    fun updateUserEmails(emails: String) {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            currentUser.trustEmails = emails
            userRepository.updateUser(currentUser)
        }
    }
    fun getCurrentGroupControl() {
        viewModelScope.launch {
            val activeControls = controlRepository.getActiveControlListByGroup()
            _currentActiveControls.value = activeControls
        }
    }

    fun getMedicadoResult(medicatedRaw: Int): String {
        return when (medicatedRaw) {
            -1 -> "No todavía"
            0 -> "No"
            1 -> "Si"
            else -> throw Exception("Valor desconocido")
        }
    }

    fun exportDataMail(items: List<Control>): String {
        val template: String = AppContext.getFileContentFromAssets("report_all.html") as String
        val content = template.replace("{fecha}", Date().customFormat("dd/MM/yyyy"))
        if (items.count() > 0) {
            var fill = ""
            var isNewGroup = false
            var ultimaFechaInicio: Date? = null

            for ((index, control) in items.withIndex()) { // o si es nuevo

                isNewGroup = if(ultimaFechaInicio == null || ultimaFechaInicio != control.startDate) true else false

                if (isNewGroup) {
                    fill += "<table class='generated'><caption>${control.startDate.customFormat("dd/MM/yyyy")} - ${control.endDate.customFormat("dd/MM/yyyy")}</caption><tr><th>Fecha</th><th>Sangre</th><th>Dosis</th><th>Recurso</th><th>Medicado</th></tr>"
                }
                fill += "<tr><td>${control.executionDate.customFormat("dd/MM/yyyy")}</td><td>${control.blood}</td><td>${control.doseLevel}</td><td>${control.resource}</td><td>${getMedicadoResult(control.medicated)}</td></tr>"

                if(index == items.count() -1)
                    fill += "</table>"
                ultimaFechaInicio = control.startDate
            }
            return content.replace("{fillable}", fill)
        } else return content.replace("{fillable}", "No hay datos")
    }

    fun prepareExportControls(externalCacheDir: File?, context: Context) {
        viewModelScope.launch {
            val items = controlRepository.getAllControls()
            val tempFile = File(externalCacheDir, "resultado.pdf")
            // Lets to transorm to PDF
            Html2Pdf.Companion.Builder()
                .context(context)
                .html(exportDataMail(items))
                .file(tempFile)
                .build().convertToPdf(object : Html2Pdf.OnCompleteConversion {
                    override fun onFailed() {
                        mExportControls.value = ""
                    }

                    override fun onSuccess() {
                        mExportControls.value = tempFile.absolutePath
                    }
                })
        }
    }
}