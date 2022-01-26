package com.quispe.coagutest.data.local

import android.content.Context
import android.content.SharedPreferences

abstract class SharedPreference {

    companion object {

        private var INSTANCE: SharedPreferences? = null
        private const val SHARED_PREFERENCES_NAME = "MySharedPreferences"

        fun getInstance(context: Context): SharedPreferences {
            if (INSTANCE == null) {
                INSTANCE = context.getSharedPreferences(
                    SHARED_PREFERENCES_NAME,
                    Context.MODE_PRIVATE
                )
            }
            return INSTANCE!!
        }
    }
}