package com.juan.prohealth.ui.ajustesActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.repository.UserRepository
import com.juan.prohealth.repository.ValidationRepository

@Suppress("UNCHECKED_CAST")
class AjustesViewModelFactory(
    private val controlRepository: ControlRepository,
    private val userRepository: UserRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        AjustesViewModel(controlRepository, userRepository) as T
}

