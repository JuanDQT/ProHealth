package com.juan.prohealth.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juan.prohealth.database.room.Control
import com.juan.prohealth.database.room.User
import com.juan.prohealth.repository.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository
) :
    ViewModel() {

    private var _bloodValue = MutableLiveData(0f)
    val bloodValue: LiveData<Float> get() = _bloodValue


    private var _controls = MutableLiveData<List<Control>>()
    val controls: LiveData<List<Control>> get() = _controls


    fun createInvitedUser(user: User) {
        viewModelScope.launch {
            userRepository.createUser(user)
        }
    }
}