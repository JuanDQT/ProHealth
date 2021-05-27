package com.juan.prohealth.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.juan.prohealth.database.daos.UserDao


@Database(entities = [User2::class], version = 1)
abstract class MyDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        private var INSTANCE: MyDatabase? = null
        private const val DATABASE_NAME = "coagutest.db"

        fun getDatabase(context: Context): MyDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext, MyDatabase::class.java, DATABASE_NAME
                ).build()
            }
            return INSTANCE!!
        }
    }
}
