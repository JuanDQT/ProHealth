package com.quispe.coagutest.ui.mainActiviy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quispe.coagutest.MyWorkManager
import com.quispe.coagutest.database.room.Control
import com.quispe.coagutest.database.room.User
import com.quispe.coagutest.repository.ControlRepository
import com.quispe.coagutest.repository.UserRepository
import com.quispe.coagutest.repository.ValidationRepository
import com.quispe.coagutest.ui.common.addDays
import com.quispe.coagutest.ui.common.clearTime
import com.quispe.coagutest.ui.common.customFormat
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(
    private val validationRepository: ValidationRepository,
    private val controlRepository: ControlRepository,
    private val userRepository: UserRepository
) :
    ViewModel() {

    private var _bloodValue = MutableLiveData(0f)
    val bloodValue: LiveData<Float> get() = _bloodValue

    private var _doseValue = MutableLiveData(0)
    val doseValue: LiveData<Int> get() = _doseValue

    private var _chipsBloodValues = MutableLiveData(emptyArray<Float>())
    val chipsBloodValues: LiveData<Array<Float>> get() = _chipsBloodValues

    private var _checkPendingControls = MutableLiveData(false)
    val checkPendingControls: LiveData<Boolean> get() = _checkPendingControls

    private var _userResourceImage = MutableLiveData(" ")
    val userResourceImage: LiveData<String> get() = _userResourceImage

    private var _currentActiveControls = MutableLiveData<List<Control>>()
    val currentActiveControls: LiveData<List<Control>> get() = _currentActiveControls

    private var _mPlanningEmails = MutableLiveData<Pair<String, String>>()
    val mPlanningEmails: LiveData<Pair<String, String>> get() = _mPlanningEmails

    init {
        getLastBloodValues()
        getDoseValue()
    }

    fun getDoseValue() {
        viewModelScope.launch {
            _doseValue.postValue(userRepository.getDoseValue())
        }
    }

    fun checkHasControlToday() {
        viewModelScope.launch {
            val hasPendingControlsQuery = controlRepository.checkIfHasPendingControlToday(isPending = -1)
            val pendingControl = controlRepository.getPendingControlToday()
            if (hasPendingControlsQuery && pendingControl != null) {
                _userResourceImage.postValue(controlRepository.getPendingControlToday()?.resource)
            }
            _checkPendingControls.postValue(hasPendingControlsQuery)
        }
    }

    fun getLastBloodValues() {
        viewModelScope.launch {
            val lastBloodValues = controlRepository.getLastBloodValues()

            _chipsBloodValues.postValue(lastBloodValues)
        }
    }

    private fun updateInfoPanelUi(user: User) {
        _bloodValue.value = user.blood
        _doseValue.value = user.level
    }

    fun updateUserData(
        bloodValue: Float,
        doseLevel: Int,
        planning: Array<String>,
        control: Control,
        sendWithEmail: Boolean
    ) {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            currentUser.blood = bloodValue
            currentUser.level = doseLevel
            userRepository.updateUser(currentUser)
            val groupControl = controlRepository.getNewIdGroup()

            addControlsToUser(
                planning,
                control,
                bloodValue,
                doseLevel,
                currentUser.id,
                groupControl
            )
            getControlsToFillCarousel()
            if (sendWithEmail)
                prepareSendPlanningEmail()
        }
    }

    private suspend fun addControlsToUser(
        resourcePlanning: Array<String>,
        control: Control,
        bloodValue: Float,
        doseLevel: Int,
        idUser: Int,
        groupControl: Int
    ) {
        for (x in 0 until resourcePlanning.size) {
            control.executionDate = Date().addDays(x).clearTime()
            control.startDate = Date().clearTime()
            control.endDate = Date().addDays(resourcePlanning.size - 1).clearTime()
            control.blood = bloodValue
            control.doseLevel = doseLevel
            control.resource = resourcePlanning[x]
            control.groupControl = groupControl
            control.idUser = idUser
            controlRepository.insert(control)
        }
    }

    fun updateCurrentControlStatus(isMedicated: Int) {
        viewModelScope.launch {
            val controlToday = controlRepository.getPendingControlToday()
            controlToday?.let { mControlToday ->
                mControlToday.medicated = isMedicated
                controlRepository.updateControl(mControlToday)
            }
        }
    }

    fun getControlsToFillCarousel() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            val activeControls = controlRepository.getActiveControlListByGroup()
            _currentActiveControls.value = activeControls
            updateInfoPanelUi(user)
            MyWorkManager.setWorkers(activeControls, user.hourAlarm, user.minuteAlarm)
        }
    }

    fun getMedicadoResult(resourceValue: String): String {
        return when (resourceValue) {
            "0" -> "No corresponde"
            else -> resourceValue
        }
    }

    fun getActiveControlListToEmail(controls: List<Control>): String {
        if (controls.count() > 0) {
            var body = "<h1>Control IRN</h1><br/><br/>"
            for (x in controls) {
                body += "<p>Fecha: ${x.executionDate.customFormat("dd/MM/yyyy")}. Dosis: ${getMedicadoResult(x.resource)}</p><br/>"
            }
            return body
        } else return "No hay datos"
    }

    fun prepareSendPlanningEmail() {
        viewModelScope.launch {
            val userEmails = userRepository.getCurrentUser().trustEmails
            val controls = controlRepository.getAllControls()
            _mPlanningEmails.postValue(Pair(getActiveControlListToEmail(controls), userEmails))
        }
    }
}