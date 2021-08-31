package com.juan.prohealth.ui.initialActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juan.prohealth.repository.UserRepository
import com.juan.prohealth.repository.ValidationRepository
import kotlinx.coroutines.launch

class InitialMainViewModel(
    private val validationRepository: ValidationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private var _existsCurrentUser = MutableLiveData<Boolean>()
    val existsCurrentUser: LiveData<Boolean> get() = _existsCurrentUser

    init {
        isLoggedCurrentUser()//logica encapsulada
    }

    private fun isLoggedCurrentUser() {
        viewModelScope.launch {
            val mExists = userRepository.isUserSuccessfulCreated()
            _existsCurrentUser.postValue(mExists)
        }
    }

    fun setFinalDate(epochDate: Long) {
        validationRepository.setFinalTestDate(epochDate)
    }

    fun saveFirstDoseLevel(doseLevel: Int) {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            currentUser.level = doseLevel
            currentUser.initialLevel = doseLevel
            userRepository.updateUser(currentUser)
        }
    }
}