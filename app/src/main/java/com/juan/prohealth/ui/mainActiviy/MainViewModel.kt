package com.juan.prohealth.ui.mainActiviy

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juan.prohealth.MyWorkManager
import com.juan.prohealth.addDays
import com.juan.prohealth.clearTime
import com.juan.prohealth.database.entity.Control
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.repository.UserRepository
import com.juan.prohealth.repository.ValidationRepository
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(
    private val validationRepository: ValidationRepository,
    private val controlRepository: ControlRepository,
    private val userRepository: UserRepository
) :
    ViewModel() {

    init {
        getActiveControlList()//Check "refresh"
    }

    private var _statusINRButton = MutableLiveData(true)
    val statusINRButton: LiveData<Boolean> get() = _statusINRButton

    private var _statusDeleteBtn = MutableLiveData(false)
    val statusDeleteBtn: LiveData<Boolean> get() = _statusDeleteBtn

    private var _bloodValue = MutableLiveData(0f)
    val bloodValue: LiveData<Float> get() = _bloodValue

    private var _doseValue = MutableLiveData(0)
    val doseValue: LiveData<Int> get() = _doseValue

    //Valores que controlan Visualizaciones?
    private var _showAlertControl = MutableLiveData(false)
    val showAlertControl: LiveData<Boolean> get() = _showAlertControl

    private var _dismissFlashBar = MutableLiveData(true)
    val dismissFlashBar: LiveData<Boolean> get() = _dismissFlashBar

    private var _visibilityGroupCarousel = MutableLiveData(0)
    val visibilityGroupCarousel: LiveData<Int> get() = _visibilityGroupCarousel

    private var _userResourceImage = MutableLiveData<String>("")
    val userResourceImage: LiveData<String> get() = _userResourceImage
    //
    private var _lastBloodValues = MutableLiveData(emptyArray<Float>())
    val lastBloodValues: LiveData<Array<Float>> get() = _lastBloodValues

    private var _controls = MutableLiveData<List<Control>>()
    val controls: LiveData<List<Control>> get() = _controls

    // New
    fun doCloseOlderControls() {
        viewModelScope.launch {
            controlRepository.updateStateToCloseControls(userRepository.getIdCurrentUser())
        }
    }

    fun deleteLastControlGroup() {
        viewModelScope.launch {
            controlRepository.deleteLastControlGroup(userRepository.getIdCurrentUser())
        }
    }

    fun getBloodValue() {
        viewModelScope.launch {
            _bloodValue.value = userRepository.getBloodValue()
        }
    }

    fun checkHasControlToday() {
        viewModelScope.launch {
            if (controlRepository.hasPendingControls(userRepository.getIdCurrentUser())) {
                _statusINRButton.value = false
                _statusDeleteBtn.value = true
                //setDosisWidget()
                _userResourceImage.value = checkDoAlertControlAndReturnResource()
                _showAlertControl.value = !_userResourceImage.value.isNullOrEmpty()
                _dismissFlashBar.value = _showAlertControl.value
            } else {
                _statusINRButton.value = true
                _statusDeleteBtn.value = false
                _visibilityGroupCarousel.value = View.GONE
                _dismissFlashBar.value = true
                MyWorkManager.clearAllWorks()
            }
        }
    }

    fun updateUserData(bloodValue: Float, level: Int) {
        viewModelScope.launch {
            userRepository.updateUserData(bloodValue, level)
        }
    }

    fun insertNewControls(planificacion: Array<String>, sangre: Float, nivel: Int) {
        viewModelScope.launch {
            for (x in 0 until planificacion.size) {
                val mControl = Control(
                    executionDate = Date().addDays(x).clearTime(),
                    startDate = Date().clearTime(),
                    endDate = Date().addDays(planificacion.size - 1).clearTime()
                )
                mControl.blood = sangre
                mControl.doseLevel = nivel
                mControl.resource = planificacion[x]
                controlRepository.insert(mControl)
            }

        }
    }

    private fun checkDoAlertControlAndReturnResource(): String {
        viewModelScope.launch {
            controlRepository.hasPedingControlToday(
                userRepository.getIdCurrentUser(),
                userRepository.getCurrentTimeNotification()
            )
        }
        return ""
    }

    fun updateCurrentControlStatus(value: Boolean) {
        viewModelScope.launch {
            controlRepository.updateCurrentControl(value, userRepository.getIdCurrentUser())
        }
    }

    fun getActiveControlList(medicated: Boolean= false) {
        viewModelScope.launch {
            val userCurrent = userRepository.getIdCurrentUser()
            _controls.value = controlRepository.getActiveControlList(userCurrent,medicated)
        }
    }
}