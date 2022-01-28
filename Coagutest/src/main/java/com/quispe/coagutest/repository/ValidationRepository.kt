package com.quispe.coagutest.repository

import com.quispe.coagutest.source.IStorageDataSource

class ValidationRepository(private val IStorageDataSource: IStorageDataSource) {

    fun getDoseLevel(): String = IStorageDataSource.getDoseLevel()

    fun setFinalTestDate(long: Long) = IStorageDataSource.setFinalTestDate(long)

    fun checkIfExist(keys: Array<String>): Boolean = IStorageDataSource.exists(keys)

    fun getBloodLevel(): String = IStorageDataSource.getBloodLevel()

    fun addString(key: String, value: String) = IStorageDataSource.addString(key, value)

    fun getString(key: String): String = IStorageDataSource.getString(key)

    fun getSystemData(): Long = IStorageDataSource.getSystemDate()

    fun updateSystemDate(value: Long) = IStorageDataSource.updateSystemDate(value)

    fun getFinalTestDate(): Long = IStorageDataSource.getFinalTestDate()
}