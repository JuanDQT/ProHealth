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
    // Probar generacion usuarios
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val name: String = "",
    val blood: Float = 0f,
    val level: Int = 0,
    @ColumnInfo(name = "hour_alarm")
    val hourAlarm: Int = 0,
    @ColumnInfo(name = "minute_alarm")
    val minuteAlarm: Int = 0,
    @ColumnInfo(name = "id_server")
    val idServer: Int = 0,
    @ColumnInfo(name = "state_logging")
    val stateLogging: Boolean = true
): Parcelable