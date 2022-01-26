package com.quispe.coagutest.ui.initialActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quispe.coagutest.repository.UserRepository

@Suppress("UNCHECKED_CAST")
class InitialModelFactory(
    private val userRepository: UserRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        InitialMainViewModel(userRepository) as T
}
