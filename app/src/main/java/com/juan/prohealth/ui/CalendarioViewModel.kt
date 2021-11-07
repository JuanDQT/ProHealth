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
import com.juan.prohealth.ui.common.fromDate
import kotlinx.coroutines.launch
import java.util.*

class CalendarioViewModel(
    private val controlRepository: ControlRepository
) : ViewModel() {

    private var _controlList = MutableLiveData<Array<Calendar>>()
    val controlList: LiveData<Array<Calendar>> get() = _controlList

    init {
        getAllControlList()
    }

    private fun getAllControlList() {
        viewModelScope.launch {
            val controls: List<Control> = controlRepository.getAllControls()
            val calendarItems: Array<Calendar> = controls.map { i -> Calendar.getInstance().fromDate(i.executionDate) }.toTypedArray()
            _controlList.postValue(calendarItems)
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