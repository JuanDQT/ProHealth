package com.juan.prohealth.data.local

import android.content.SharedPreferences
import com.juan.prohealth.source.StorageDataSource

class StorageValidationDataSource(private val sharedPreferences: SharedPreferences) :
    StorageDataSource {

    companion object{
        // KEYS
        private const val LOGGED_CURRENT_USER = "LOGGED_CURRENT_USER"
        private const val SYSTEM_DATE = "SYSTEM_DATE"
        private const val END_DATE_TEST = "END_DATE_TEST"
    }

    override fun exists(keys: Array<String>): Boolean {

        if (keys.count() < 1)
            return false

        sharedPreferences?.let {sharedPreferences->
            for (key in keys) {
                if (!sharedPreferences.contains(key))
                    return false
            }
            return true
        }
        return false
    }

    override fun getDoseLevel(): String {
        sharedPreferences?.let {
            return it.getString("nivel", "0")
        }
        return "0"
    }

    override fun getBloodLevel(): String {
        sharedPreferences?.let {
            return it.getString("sangre", "0")
        }
        return "0"
    }

    override fun addString(key: String, value: String) {
        sharedPreferences?.let {
            val editor = it.edit()
            editor.putString(key, value)
            editor.apply()
        }
    }

    override fun getString(key: String): String {
        val defaultValue = ""
        sharedPreferences?.let {
            return it.getString(key, defaultValue)
        }
        return defaultValue
    }

    override fun getSystemDate(): Long {
        sharedPreferences.let {
            return it.getLong(SYSTEM_DATE, 0)
        }
        return 0
    }

    override fun updateSystemDate(value: Long) {
        sharedPreferences?.let {
            val editor = it.edit()
            editor.putLong(SYSTEM_DATE, value)
            editor.apply()
        }
    }

    override fun getFinalTestDate(): Long {
        sharedPreferences?.let {
            return it.getLong(END_DATE_TEST, 0)
        }
        return 0
    }

    override fun setFinalTestDate(value: Long) {
        sharedPreferences?.let {
            val editor = it.edit()
            editor.putLong(END_DATE_TEST, value)
            editor.apply()
        }
    }
}