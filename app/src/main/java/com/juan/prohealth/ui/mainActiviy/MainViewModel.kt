package com.juan.prohealth.ui.mainActiviy

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juan.prohealth.MyWorkManager
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

    private var _bloodValue = MutableLiveData(0f)
    val bloodValue: LiveData<Float> get() = _bloodValue

    private var _doseValue = MutableLiveData(0)
    val doseValue: LiveData<Int> get() = _doseValue

    private var _checkPendingControls = MutableLiveData(false)
    val checkPendingControls: LiveData<Boolean> get() = _checkPendingControls

    private var _userResourceImage = MutableLiveData(" ")
    val userResourceImage: LiveData<String> get() = _userResourceImage

    private var _currentActiveControls = MutableLiveData<List<Control>>()
    val currentActiveControls: LiveData<List<Control>> get() = _currentActiveControls

    fun deleteLastControlGroup() {
        viewModelScope.launch {
            Log.i("BTN REBOOT INR", "Se procede a borrar el ultimo grupo de control")
            controlRepository.deleteLastControlGroup(userRepository.getIdCurrentUser())
        }
    }

    fun checkHasControlToday() {
        viewModelScope.launch {
            val hasPendingControlsQuery =
                controlRepository.checkIfHasPendingControlToday(isPending = -1)

            if (hasPendingControlsQuery) {
                _userResourceImage.postValue(controlRepository.getPendingControlToday().resource)
            }
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
        planning: Array<String>,
        control: Control
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
            setDefaultNotifications()
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
            if (isMedicated == 1) {
                val controlToday = controlRepository.getPendingControlToday()
                controlToday.medicated = isMedicated
                controlRepository.updateControl(controlToday)
            }
        }
    }

    fun getControlsToFillCarousel() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            val activeControls = controlRepository.getActiveControlListByGroup()
            _currentActiveControls.value = activeControls
            updateInfoPanelUi(user)
        }
    }

    private fun setDefaultNotifications() {
        viewModelScope.launch {
            val activeControls = controlRepository.getActiveControlListByGroup()
            MyWorkManager.setWorkers(activeControls)
        }
    }
}