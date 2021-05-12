package com.juan.prohealth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.juan.prohealth.repository.ValidationRepository

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(private val validationRepository: ValidationRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        MainViewModel(validationRepository) as T
}
