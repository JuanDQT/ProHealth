package com.quispe.coagutest.ui.initialActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quispe.coagutest.database.room.User
import com.quispe.coagutest.repository.UserRepository
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