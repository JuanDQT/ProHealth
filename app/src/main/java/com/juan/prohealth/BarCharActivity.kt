package com.juan.prohealth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.anychart.graphics.vector.Stroke
import kotlinx.android.synthetic.main.activity_bar_char.*
import java.util.*
import kotlin.collections.ArrayList

class BarCharActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bar_char)
        title = ""
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(true);

        acv_grafica.setProgressBar(findViewById(R.id.progress_bar))

        val cartesian = AnyChart.line()

        cartesian.animation(true)

        cartesian.padding(10.0, 20.0, 5.0, 20.0)

        cartesian.crosshair().enabled(true)
        cartesian.crosshair()
            .yLabel(true) // TODO ystroke
            .yStroke(
                null as Stroke?,
                null,
                null,
                null as String?,
                null as String?
            )

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)

        cartesian.title("Seguimiento de dosis en los ultimos 30 dias")

        cartesian.yAxis(0).title("Cantidad (porciones)")
        cartesian.xAxis(0).labels().padding(5.0, 5.0, 5.0, 5.0)

        val seriesData: MutableList<DataEntry> = getDemoPoints()

        val set = Set.instantiate()
        set.data(seriesData)
        val series1Mapping = set.mapAs("{ x: 'x', value: 'value' }")

        val series1 = cartesian.line(series1Mapping)
        series1.name("Ocultar esto")
        series1.hovered().markers().enabled(true)
        series1.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series1.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)

        cartesian.legend().enabled(true)
        cartesian.legend().fontSize(13.0)
        cartesian.legend().padding(0.0, 0.0, 10.0, 0.0)

        acv_grafica.setChart(cartesian)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // TODO: mirar la forma de representar los valores decimales en fracciones.. API
    fun getDemoPoints(): MutableList<DataEntry> {
        val list = arrayListOf<DataEntry>()
        val calendar = Calendar.getInstance()
        val dosis = floatArrayOf(0f, 0.125f, 0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f, 2.25f)
        for (i in 0..30) {
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            list.add(ValueDataEntry(calendar.get(Calendar.DAY_OF_MONTH), dosis[(0 until dosis.count()).random()]))
        }

        return list.asReversed()
    }
}
