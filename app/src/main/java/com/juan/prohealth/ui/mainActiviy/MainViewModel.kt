package com.juan.prohealth.ui.mainActiviy

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

    private var _checkPendingControls = MutableLiveData(false)
    val checkPendingControls: LiveData<Boolean> get() = _checkPendingControls

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

    fun checkHasControlToday() {
        viewModelScope.launch {
            val hasPendingControlsQuery =
                controlRepository.checkIfHasPendingControlToday(isPending = 0)
            _checkPendingControls.postValue(hasPendingControlsQuery)
        }
    }

    private fun updateInfoPanelUi(user: User) {
        _bloodValue.value = user.blood
        _doseValue.value = user.level
    }

    fun updateUserData(
        bloodValue: Float,
        doseLevel: Int,
        planificacion: Array<String>,
        control: Control
    ) {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            currentUser.blood = bloodValue
            currentUser.level = doseLevel
            userRepository.updateUser(currentUser)
            val groupControl = controlRepository.getNewIdGroup()

            addControlsToUser(
                planificacion,
                control,
                bloodValue,
                doseLevel,
                currentUser.id,
                groupControl
            )
            getActiveControlList()
        }
    }

    private suspend fun addControlsToUser(
        resourcePlanification: Array<String>,
        control: Control,
        bloodValue: Float,
        doseLevel: Int,
        idUser: Int,
        groupControl: Int
    ) {
        for (x in 0 until resourcePlanification.size) {
            control.executionDate = Date().addDays(x).clearTime()
            control.startDate = Date().clearTime()
            control.endDate = Date().addDays(resourcePlanification.size - 1).clearTime()
            control.blood = bloodValue
            control.doseLevel = doseLevel
            control.resource = resourcePlanification[x]
            control.groupControl = groupControl
            control.idUser = idUser
            controlRepository.insert(control)
        }
    }

    private fun checkDoAlertControlAndReturnResource(): String {
        viewModelScope.launch {
            /*    controlRepository.hasPedingControlToday(
                    userRepository.getCurrentUser()
                )*/
            TODO()
        }
        return ""
    }

    fun updateCurrentControlStatus(isMedicated: Boolean) {
        viewModelScope.launch {
            if (isMedicated){
                val controlToday = controlRepository.getPendingControlToday()
                controlToday.medicated = isMedicated
                controlRepository.updateControl(controlToday)
            }
        }
    }

    fun getActiveControlList() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            val activeControls = controlRepository.getActiveControlListByGroup(user.id)
            _currentActiveControls.value = activeControls
            updateInfoPanelUi(user)
        }
    }
}