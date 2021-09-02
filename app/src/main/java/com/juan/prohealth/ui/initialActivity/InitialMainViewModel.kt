package com.juan.prohealth.ui.initialActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juan.prohealth.database.room.User
import com.juan.prohealth.repository.UserRepository
import kotlinx.coroutines.launch

class InitialMainViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    fun createUser(user: User) {
        viewModelScope.launch {
            userRepository.create(user)
        }
    }
}