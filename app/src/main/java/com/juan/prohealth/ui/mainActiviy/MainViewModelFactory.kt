package com.juan.prohealth.ui.mainActiviy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.juan.prohealth.database.UserRepo
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.repository.ValidationRepository

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(
    private val userRepo: UserRepo,
    private val controlRepository: ControlRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        MainViewModel(userRepo, controlRepository) as T
}
