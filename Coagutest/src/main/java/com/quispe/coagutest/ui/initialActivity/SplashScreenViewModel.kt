package com.quispe.coagutest.ui.initialActivity

import androidx.lifecycle.*
import com.quispe.coagutest.repository.UserRepository
import kotlinx.coroutines.launch

class SplashScreenViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private var _existsCurrentUser = MutableLiveData<Boolean>()
    val existsCurrentUser: LiveData<Boolean> get() = _existsCurrentUser

    init {
        isLoggedCurrentUser()
    }

    private fun isLoggedCurrentUser() {
        viewModelScope.launch {
            val mExists = userRepository.isUserSuccessfulCreated()
            _existsCurrentUser.postValue(mExists)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class SplashScreenFactory(
    private val userRepository: UserRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        SplashScreenViewModel(userRepository) as T
}
