package com.juan.prohealth.repository

import com.juan.prohealth.source.IStorageDataSource

class ValidationRepository(private val iStorageDataSource: IStorageDataSource) {

    fun getDoseLevel(): String = iStorageDataSource.getDoseLevel()

    fun setFinalTestDate(long: Long) = iStorageDataSource.setFinalTestDate(long)

    fun checkIfExist(keys: Array<String>): Boolean = iStorageDataSource.exists(keys)

    fun getBloodLevel(): String = iStorageDataSource.getBloodLevel()

    fun addString(key: String, value: String) = iStorageDataSource.addString(key, value)

    fun getString(key: String): String = iStorageDataSource.getString(key)

    fun getSystemData(): Long = iStorageDataSource.getSystemDate()

    fun updateSystemDate(value: Long) = iStorageDataSource.updateSystemDate(value)

    fun getFinalTestDate(): Long = iStorageDataSource.getFinalTestDate()
}