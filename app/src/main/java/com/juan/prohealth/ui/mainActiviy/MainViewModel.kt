package com.juan.prohealth.ui.mainActiviy

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juan.prohealth.database.room.Control
import com.juan.prohealth.database.room.User
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.repository.UserRepository
import com.juan.prohealth.repository.ValidationRepository
import com.juan.prohealth.ui.common.addDays
import com.juan.prohealth.ui.common.clearTime
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(
    private val validationRepository: ValidationRepository,
    private val controlRepository: ControlRepository,
    private val userRepository: UserRepository
) :
    ViewModel() {

    init {
        getActiveControlList()//"refresh"
    }

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

    private var _userResourceImage = MutableLiveData(" ")
    val userResourceImage: LiveData<String> get() = _userResourceImage
    //

    private var _currentActiveControls = MutableLiveData<List<Control>>()
    val currentActiveControls: LiveData<List<Control>> get() = _currentActiveControls

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

    fun checkHasControlTodayNew() {
        TODO()
    }

    fun checkHasControlToday() {
        viewModelScope.launch {
            var idUser = userRepository.getIdCurrentUser()
            var state = controlRepository.hasPendingControls(idUser)

            if (state) {
                _statusDeleteBtn.value = true
                //setDosisWidget()
                _userResourceImage.value = checkDoAlertControlAndReturnResource()
                _showAlertControl.value = !_userResourceImage.value.isNullOrEmpty()
                _dismissFlashBar.value = _showAlertControl.value
            } else {
                _statusDeleteBtn.value = false
                _visibilityGroupCarousel.value = View.GONE
                _dismissFlashBar.value = true
                // MyWorkManager.clearAllWorks()
            }
        }
    }

    private fun updateInfoPanelUi(user: User) {
        _bloodValue.value = user.blood
        _doseValue.value = user.level
    }

    fun updateUserData(
        bloodValue: Float,
        nivel: Int,
        planificacion: Array<String>,
        control: Control
    ) {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            currentUser.blood = bloodValue
            currentUser.level = nivel
            userRepository.updateUser(currentUser)

            addControlsToUser(planificacion, control, bloodValue, nivel, currentUser)
            getActiveControlList()
        }
    }

    private suspend fun addControlsToUser(
        planificacion: Array<String>,
        control: Control,
        bloodValue: Float,
        nivel: Int,
        currentUser: User
    ) {
        for (x in 0 until planificacion.size) {
            control.executionDate = Date().addDays(x).clearTime()
            control.startDate = Date().clearTime()
            control.endDate = Date().addDays(planificacion.size - 1).clearTime()
            control.blood = bloodValue
            control.doseLevel = nivel
            control.resource = planificacion[x]
            control.idUser = currentUser.id
            controlRepository.insert(control)
        }
    }

    private fun checkDoAlertControlAndReturnResource(): String {
        viewModelScope.launch {
            controlRepository.hasPedingControlToday(
                userRepository.getCurrentUser()
            )
        }
        return ""
    }

    fun updateCurrentControlStatus(isMedicated: Boolean) {
        viewModelScope.launch {
            //controlRepository.updateCurrentControl(value, userRepository.getIdCurrentUser())
            val controlToday = controlRepository.getControlByDate(Date().clearTime())
            controlToday.medicated = isMedicated
            controlRepository.updateControl(controlToday)
        }
    }

    fun getActiveControlList() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            val activeControls = controlRepository.getActiveControlList(user.id)
            _currentActiveControls.value = activeControls
            updateInfoPanelUi(user)
        }
    }
}