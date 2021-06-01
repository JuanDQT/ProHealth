package com.juan.prohealth.database.entity

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
    var id: Int,
    val name: String,
    val blood: Float,
    val level: Int,
    @ColumnInfo(name = "hour_alarm")
    val hourAlarm: Int,
    @ColumnInfo(name = "minute_alarm")
    val minuteAlarm: Int,
    @ColumnInfo(name = "id_server")
    val idServer: Int
): Parcelable