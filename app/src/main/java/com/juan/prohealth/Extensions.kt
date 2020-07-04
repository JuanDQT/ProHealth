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

fun Activity.print(message: String) {
    Log.i("LOG", message)
}

fun Date.customFormat(): String {
    val formatter = SimpleDateFormat("dd/MM")
    return formatter.format(this)
}

fun Activity.alert(title: String = "Alerta", message: String) {
    val dialogBuilder = AlertDialog.Builder(this)
    dialogBuilder.setTitle(title)
    dialogBuilder.setMessage(message)
    dialogBuilder.setPositiveButton("Aceptar", null)

    dialogBuilder.create().show()
}