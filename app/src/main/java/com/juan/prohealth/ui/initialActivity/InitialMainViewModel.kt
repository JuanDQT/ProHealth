package com.juan.prohealth.ui.firstActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.juan.prohealth.repository.ValidationRepository

class InitialMainViewModel(private val validationRepository: ValidationRepository) : ViewModel() {

    private var _isStored = MutableLiveData<Boolean>()
    val isStored: LiveData<Boolean> get() = _isStored

    fun isInSharedPreferences(): Boolean {
        return if (validationRepository.checkIfExist(arrayOf("nivel"))) true
        else return false
    }

    fun isInSharedPreferencesNew() {
        _isStored.value = validationRepository.checkIfExist(arrayOf("nivel"))
    }
}