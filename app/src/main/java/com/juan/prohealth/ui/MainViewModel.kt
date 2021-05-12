package com.juan.prohealth.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.juan.prohealth.repository.ValidationRepository

class MainViewModel(private val validationRepository: ValidationRepository) :
    ViewModel() {

    private var _rangeFile = MutableLiveData<String>()
    val rangeFile: LiveData<String> get() = _rangeFile

}