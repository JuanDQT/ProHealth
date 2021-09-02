package com.juan.prohealth.ui

import androidx.lifecycle.ViewModel
import com.juan.prohealth.repository.UserRepository

class LoginViewModel(
    private val userRepository: UserRepository
) :
    ViewModel()