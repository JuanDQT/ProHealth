package com.juan.prohealth.database.room

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
    var executionDate: Date = Calendar.getInstance().time,
    @ColumnInfo(name = "start_date")
    var startDate: Date = Calendar.getInstance().time,
    @ColumnInfo(name = "end_date")
    var endDate: Date = Calendar.getInstance().time,
    var resource: String = "",
    /*
    -1  No seteado(instancia)
    0   No medicado(expresamente)
    1   Medicado(expresamente)
     */
    var medicated: Int = -1,
    @ColumnInfo(name = "group_control")
    var groupControl: Int = 0
): Parcelable