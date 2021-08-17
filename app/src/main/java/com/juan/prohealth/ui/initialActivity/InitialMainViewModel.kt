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

    fun isInSharedPreferences(doseLevelName: Array<String>): Boolean {
        return if (validationRepository.checkIfExist(doseLevelName)) true
        else return false
    }

    fun setFinalDate(epochDate: Long) {
        validationRepository.setFinalTestDate(epochDate)
    }

    fun addString(key: String, value: String) {
        validationRepository.addString(key, value)
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