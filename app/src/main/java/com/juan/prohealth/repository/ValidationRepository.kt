package com.juan.prohealth.repository

import com.juan.prohealth.source.StorageDataSource

class ValidationRepository(private val storageDataSource: StorageDataSource) {

    fun getDoseLevel(): String = storageDataSource.getDoseLevel()

    fun setFinalTestDate(long: Long) = storageDataSource.setFinalTestDate(long)

    fun checkIfExist(keys: Array<String>): Boolean = storageDataSource.exists(keys)

    fun getBloodLevel(): String = storageDataSource.getBloodLevel()

    fun addString(key: String, value: String) = storageDataSource.addString(key, value)

    fun getString(key: String): String = storageDataSource.getString(key)

    fun getSystemData(): Long = storageDataSource.getSystemDate()

    fun updateSystemDate(value: Long) = storageDataSource.updateSystemDate(value)

    fun getFinalTestDate(): Long = storageDataSource.getFinalTestDate()
}