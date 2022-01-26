package com.quispe.coagutest.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quispe.coagutest.repository.UserRepository

@Suppress("UNCHECKED_CAST")
class LoginViewModelFactory(
    private val userRepository: UserRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        LoginViewModel(userRepository) as T
}

