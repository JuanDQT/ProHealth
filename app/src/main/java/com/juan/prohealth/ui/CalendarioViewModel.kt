package com.juan.prohealth.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.events.calendar.views.EventsCalendar
import com.juan.prohealth.database.room.Control
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.ui.common.clearTime
import com.juan.prohealth.ui.common.fromDate
import kotlinx.coroutines.launch
import java.util.*

class CalendarioViewModel(
    private val controlRepository: ControlRepository
) : ViewModel(), EventsCalendar.Callback {

    private var _controlList = MutableLiveData<Array<Calendar>>()
    private var _controlSelected = MutableLiveData<Control?>()
    val controlList: LiveData<Array<Calendar>> get() = _controlList
    val controlSelected: LiveData<Control?> get() = _controlSelected

    init {
        getAllControlList()
    }

    private fun getAllControlList() {
        viewModelScope.launch {
            val controls: List<Control> = controlRepository.getAllControls()
            val calendarItems: Array<Calendar> = controls.map { i -> Calendar.getInstance().fromDate(i.executionDate) }.toTypedArray()
            _controlList.postValue(calendarItems)
            onDaySelected(Calendar.getInstance())
        }
    }

    override fun onDaySelected(selectedDate: Calendar?) {
        selectedDate?.let { calendar ->
            viewModelScope.launch {
                val controlAtDate = controlRepository.getControlByDate(calendar.time.clearTime())
                _controlSelected.postValue(controlAtDate)
            }
        }
    }

    override fun onDayLongPressed(selectedDate: Calendar?) {}

    override fun onMonthChanged(monthStartDate: Calendar?) {}

}