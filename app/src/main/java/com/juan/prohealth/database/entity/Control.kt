package com.juan.prohealth.database.entity

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(foreignKeys = arrayOf(ForeignKey(entity = Control::class,
    parentColumns = arrayOf("id"),
    childColumns = arrayOf("user_id"),
    onDelete = ForeignKey.CASCADE)))
@Parcelize
data class Control(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "user_id")
    var idUser: Int = 0,
    var blood: Float = 0f,
    @ColumnInfo(name = "dose_level")
    var doseLevel: Int = 0,
    @ColumnInfo(name = "execution_date")
    val executionDate: Date,
    @ColumnInfo(name = "start_date")
    val startDate: Date,
    @ColumnInfo(name = "end_date")
    val endDate: Date,
    var resource: String = "",
    var medicated: Boolean = false,
    @ColumnInfo(name = "group_control")
    var groupControl: Int = 0
): Parcelable