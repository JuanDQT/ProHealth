package com.juan.prohealth.ui.ajustesActivity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juan.prohealth.AppContext
import com.juan.prohealth.database.room.Control
import com.juan.prohealth.database.room.User
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.repository.UserRepository
import kotlinx.coroutines.launch

class AjustesViewModel(
    private val controlRepository: ControlRepository,
    private val userRepository: UserRepository
): ViewModel() {

    private var _userSchedule = MutableLiveData(emptyArray<Int>())
    val userSchedule: LiveData<Array<Int>> get() = _userSchedule

    private var _currentActiveControls = MutableLiveData<List<Control>>()
    val currentActiveControls: LiveData<List<Control>> get() = _currentActiveControls

    private var hourUser: Int? = 0
    private var minuteUser: Int? = 0

    init {
        getUserSchedule()
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

    fun getUserSchedule() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            hourUser = user.hourAlarm
            minuteUser = user.minuteAlarm
            _userSchedule.postValue(arrayOf(user.hourAlarm, user.minuteAlarm))
        }
    }

    fun updateUserSchedule(hour: Int, minute: Int) {
        viewModelScope.launch {
            userRepository.updateUserSchedule(hour, minute)
        }
    }

    fun getCurrentGroupControl() {
        viewModelScope.launch {
            val activeControls = controlRepository.getActiveControlListByGroup()
            _currentActiveControls.value = activeControls
        }
    }
}