package com.quispe.coagutest

import android.content.Context

class MySharedPreferences {

    object shared {
        private val SHARED_PREFERENCES_NAME = "MySharedPreferences"
        private var sharedPreferences = AppContext.context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        // KEYS
        val LOGGED_CURRENT_USER = "LOGGED_CURRENT_USER"
        val SYSTEM_DATE = "SYSTEM_DATE"
        val END_DATE_TEST = "END_DATE_TEST"

        fun exists(keys: Array<String>): Boolean {
            if(keys.count() < 1)
                return false

            sharedPreferences?.let {
                for (key in keys) {
                    if(!it.contains(key))
                        return false
                }
                return true
            }
            return false

        }

        fun getNivel(): String {
           sharedPreferences?.let {
               return it.getString("nivel", "0") ?: ""
           }
            return "0"
        }

        fun getSangre(): String {
            sharedPreferences?.let {
                return it.getString("sangre", "0") ?: ""
            }
            return "0"
        }


        // genericos
        fun addString(key: String, value: String) {
            sharedPreferences?.let {
                val editor = it.edit()
                editor.putString(key, value)
                editor.apply()
            }
        }

        // genericos
        fun getString(key: String): String {
            sharedPreferences?.let {
                return it.getString(key, "") ?: ""
            }
            return ""
        }

        fun getSystemDate(): Long {
            sharedPreferences?.let {
                return it.getLong(SYSTEM_DATE, 0)
            }
            return 0
        }

        fun updateSystemDate(value: Long) {
            sharedPreferences?.let {
                val editor = it.edit()
                editor.putLong(SYSTEM_DATE, value)
                editor.apply()
            }
        }

        fun getFechaFinPrueba(): Long {
            sharedPreferences?.let {
                return it.getLong(END_DATE_TEST, 0)
            }
            return 0
        }

        fun setFechaFinPrueba(value: Long) {
            sharedPreferences?.let {
                val editor = it.edit()
                editor.putLong(END_DATE_TEST, value)
                editor.apply()
            }
        }

    }
}