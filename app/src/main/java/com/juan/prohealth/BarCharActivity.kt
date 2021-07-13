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
import com.juan.prohealth.database.Control
import com.juan.prohealth.databinding.ActivityBarCharBinding
import com.juan.prohealth.ui.common.customFormat

class BarCharActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBarCharBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarCharBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.acvGrafica.setProgressBar(findViewById(R.id.progress_bar))

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

        cartesian.title().enabled(false)

        cartesian.yAxis(0).title(getString(R.string.valores_inr))
        cartesian.xAxis(0).labels().padding(5.0, 5.0, 5.0, 5.0).rotation(270).fontWeight("bold")
        cartesian.yAxis(0).labels().fontWeight("bold")
        cartesian.xAxis(0).title(getString(R.string.fecha))
        cartesian.yGrid(true)


        val seriesData: MutableList<DataEntry> = getDemoPoints()
        val color = resources.getString(R.color.colorPrimary).replace("ff", "").toUpperCase()

        val set = Set.instantiate()
        set.data(seriesData)
        val series1Mapping = set.mapAs("{ x: 'x', value: 'value' }")


        val series1 = cartesian.line(series1Mapping)
        series1.hovered().markers().enabled(true)
        series1.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series1.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0).enabled(false)
        series1.color(color)
        // TODO; hacer la linea mas gruesa...
        series1.stroke("7 $color").enabled(true)
        series1.color(color).markers().enabled(true)
            .type(MarkerType.CIRCLE)
            .size(9)
        series1.hover().labels(false)




        cartesian.legend().enabled(false)
        cartesian.legend().fontSize(13.0)
        cartesian.legend().padding(0.0, 0.0, 10.0, 0.0)

        binding.acvGrafica.setChart(cartesian)
    }

    fun getDemoPoints(): MutableList<DataEntry> {
        val list = arrayListOf<DataEntry>()

        Control.getHistoric().let { controles ->
            if (controles.count() > 0) {
                for (item in controles) {
                    list.add(ValueDataEntry(item.fechaInicio?.customFormat("dd/MM"), item.sangre))
                }
            }
        }
        return list
    }
}
