package com.juan.prohealth.database.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(tableName = "control")
@Parcelize
data class Control(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    val blood: Float,
    @ColumnInfo(name = "dose_level")
    val doseLevel: Float,
    @ColumnInfo(name = "execution_date")
    val executionDate: Date,
    @ColumnInfo(name = "start_date")
    val startDate: Date,
    @ColumnInfo(name = "end_date")
    val endDate: Date,
    val resource: String,
    val medicado: Boolean
): Parcelable