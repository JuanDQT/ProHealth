package com.quispe.coagutest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quispe.coagutest.repository.ControlRepository
import com.quispe.coagutest.ui.GraphViewModel

@Suppress("UNCHECKED_CAST")
class GraphViewModelFactory(
    private val controlRepository: ControlRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        GraphViewModel(controlRepository) as T
}

