package com.quispe.coagutest.ui

import androidx.lifecycle.ViewModel
import com.quispe.coagutest.repository.UserRepository

class LoginViewModel(
    private val userRepository: UserRepository
) :
    ViewModel()