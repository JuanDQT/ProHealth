package com.quispe.coagutest.ui.ajustesActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quispe.coagutest.repository.ControlRepository
import com.quispe.coagutest.repository.UserRepository

@Suppress("UNCHECKED_CAST")
class AjustesViewModelFactory(
    private val controlRepository: ControlRepository,
    private val userRepository: UserRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        AjustesViewModel(controlRepository, userRepository) as T
}

