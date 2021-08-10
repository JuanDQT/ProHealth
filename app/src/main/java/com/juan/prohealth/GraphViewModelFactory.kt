package com.juan.prohealth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.repository.UserRepository
import com.juan.prohealth.repository.ValidationRepository
import com.juan.prohealth.ui.GraphViewModel

@Suppress("UNCHECKED_CAST")
class GraphViewModelFactory(
    private val controlRepository: ControlRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        GraphViewModel(controlRepository) as T
}

