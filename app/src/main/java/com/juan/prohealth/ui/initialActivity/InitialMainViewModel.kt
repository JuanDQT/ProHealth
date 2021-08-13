package com.juan.prohealth.ui.initialActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juan.prohealth.database.room.User
import com.juan.prohealth.repository.UserRepository
import com.juan.prohealth.repository.ValidationRepository
import kotlinx.coroutines.launch

class InitialMainViewModel(
    private val validationRepository: ValidationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private var _isStored = MutableLiveData<Boolean>()
    val isStored: LiveData<Boolean> get() = _isStored

    private var _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser

    init {
        viewModelScope.launch {
            val data = userRepository.getUserSuccesfulCreated()
            _currentUser.postValue(data)
        }
    }

    fun setFinalDate(epochDate: Long) {
        validationRepository.setFinalTestDate(epochDate)
    }

    fun saveFirstDoseLevel(doseLevel: Int) {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            currentUser.level = doseLevel
            userRepository.updateUser(currentUser)
        }
    }
}