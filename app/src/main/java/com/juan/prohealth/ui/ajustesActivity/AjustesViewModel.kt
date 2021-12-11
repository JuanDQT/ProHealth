package com.juan.prohealth.ui.ajustesActivity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juan.prohealth.MyWorkManager
import com.juan.prohealth.database.room.Control
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.repository.UserRepository
import kotlinx.coroutines.launch

class AjustesViewModel(
    private val controlRepository: ControlRepository,
    private val userRepository: UserRepository
): ViewModel() {

    private var mUserAlarm = MutableLiveData<Pair<Int, Int>>()
    private var mUserAlarmForAlert = MutableLiveData<Pair<Int, Int>>()

    private var _currentActiveControls = MutableLiveData<List<Control>>()
    val currentActiveControls: LiveData<List<Control>> get() = _currentActiveControls

    private var mUserEmailInfo = MutableLiveData<String?>()

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
}