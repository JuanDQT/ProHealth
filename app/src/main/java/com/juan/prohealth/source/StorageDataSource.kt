package com.juan.prohealth.source

interface StorageDataSource {
    fun exists(keys: Array<String>): Boolean
    fun getDoseLevel(): String
    fun getBloodLevel(): String
    fun addString(key: String, value: String)
    fun getString(key: String): String
    fun getSystemDate(): Long
    fun updateSystemDate(value: Long)
    fun getFinalTestDate(): Long
    fun setFinalTestDate(value: Long)
}