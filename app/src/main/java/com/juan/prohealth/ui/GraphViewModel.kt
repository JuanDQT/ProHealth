package com.juan.prohealth.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.juan.prohealth.database.room.Control
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.ui.common.customFormat
import kotlinx.coroutines.launch
import java.util.*

class GraphViewModel(
    private val controlRepository: ControlRepository
) : ViewModel() {

    private var _controlList = MutableLiveData<MutableList<DataEntry>>()
    val controlList: LiveData<MutableList<DataEntry>> get() = _controlList

    init {
        getControlList()
    }

    private fun getControlList() {
        viewModelScope.launch {
            val controls = controlRepository.getControlListGraph()
            val controlsEntry = getDataEntryFromlist(controls)
            _controlList.postValue(controlsEntry)
        }
    }

    private fun getDataEntryFromlist(list: List<Control>): MutableList<DataEntry> {
        val listEntry = arrayListOf<DataEntry>()

        for (item in list) {
            listEntry.add(ValueDataEntry(item.startDate.customFormat("dd/MM"), item.blood))
        }

        return listEntry
    }

}