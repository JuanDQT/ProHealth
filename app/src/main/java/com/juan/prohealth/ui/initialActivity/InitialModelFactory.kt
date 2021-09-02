package com.juan.prohealth.ui.initialActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.juan.prohealth.repository.UserRepository
import com.juan.prohealth.repository.ValidationRepository

@Suppress("UNCHECKED_CAST")
class InitialModelFactory(
    private val userRepository: UserRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        InitialMainViewModel(userRepository) as T
}
