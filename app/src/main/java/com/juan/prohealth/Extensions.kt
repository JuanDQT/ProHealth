package com.juan.prohealth

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*


fun AppCompatActivity.hideKeyboard() {
    val view = this.currentFocus
    if (view != null) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    // else {
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    // }
}

// Compatibilidad con versiones antiguas de android
fun Date.addDays(days: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return calendar.time
}

fun Date.clearTime(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar[Calendar.HOUR_OF_DAY] = 0
    calendar[Calendar.MINUTE] = 0
    calendar[Calendar.SECOND] = 0
    calendar[Calendar.MILLISECOND] = 0

    return calendar.time
}

fun Activity.print(message: String) {
    Log.i("LOG", message)
}

fun Date.customFormat(format: String): String {
    val formatter = SimpleDateFormat(format)
    return formatter.format(this)
}

fun Activity.alert(title: String = "Alerta", message: String) {
    val dialogBuilder = AlertDialog.Builder(this)
    dialogBuilder.setTitle(title)
    dialogBuilder.setMessage(message)
    dialogBuilder.setPositiveButton("Aceptar", null)

    dialogBuilder.create().show()
}

fun Calendar.fromDate(date: Date): Calendar {
    this.time = date
    return this
}

fun Calendar.fromDate(date: Date, hora: Int, minuto: Int): Calendar {
    this.time = date
    this[Calendar.HOUR_OF_DAY] = hora
    this[Calendar.MINUTE] = minuto
    this[Calendar.SECOND] = 0
    return this
}

fun Long.clearSeconds(): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    cal[Calendar.SECOND] = 0
    return cal.timeInMillis
}