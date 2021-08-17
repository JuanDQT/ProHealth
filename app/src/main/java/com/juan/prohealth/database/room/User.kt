package com.juan.prohealth.database.room

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "user", indices = [Index(value = ["id_server"], unique = true)])
@Parcelize
data class User(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String = "",
    var blood: Float = 0f,
    var level: Int = 0,
    @ColumnInfo(name="initial_level")
    var initialLevel: Int = 0,
    @ColumnInfo(name = "hour_alarm")
    var hourAlarm: Int = 7,
    @ColumnInfo(name = "minute_alarm")
    var minuteAlarm: Int = 0,
    @ColumnInfo(name = "id_server")
    var idServer: Int = 0,
    @ColumnInfo(name = "state_logging")
    var stateLogging: Boolean = true
): Parcelable