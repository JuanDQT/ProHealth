package com.quispe.coagutest.ui.mainActiviy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quispe.coagutest.repository.ControlRepository
import com.quispe.coagutest.repository.UserRepository
import com.quispe.coagutest.repository.ValidationRepository

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(
    private val validationRepository: ValidationRepository,
    private val controlRepository: ControlRepository,
    private val userRepository: UserRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        MainViewModel(validationRepository, controlRepository, userRepository) as T
}

