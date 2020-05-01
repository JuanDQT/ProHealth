package com.juan.prohealth

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.Utils
import kotlinx.android.synthetic.main.activity_estadisticas.*
import java.util.*


class EstadisticasActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadisticas)

        btnMensual.setOnClickListener(this)
        btnSemestral.setOnClickListener(this)
        btnAnual.setOnClickListener(this)

        // enable touch gestures
        chart.setTouchEnabled(false)
        // create marker to display box when values are selected

        // create marker to display box when values are selected
        val mv = MarkerView(this, R.layout.custom_marker_view)

        // Set the marker to the chart

        // Set the marker to the chart
        mv.setChartView(chart)
        chart.marker = mv

        var yAxis = chart.axisLeft

        // disable dual axis (only use LEFT axis)
        chart.axisRight.isEnabled = false

        // horizontal grid lines
        yAxis.enableGridDashedLine(10f, 10f, 0f)

        // axis range
        yAxis.axisMaximum = 60f
        yAxis.axisMinimum = -0f


        // // Create Limit Lines // //
        val llXAxis = LimitLine(9f, "Index 10")
        llXAxis.lineWidth = 4f
        llXAxis.enableDashedLine(10f, 10f, 0f)
        llXAxis.labelPosition = LimitLabelPosition.RIGHT_BOTTOM
        llXAxis.textSize = 10f

        val ll1 = LimitLine(50f, "Maximo registrado")
        ll1.lineWidth = 4f
        ll1.enableDashedLine(10f, 10f, 0f)
        ll1.labelPosition = LimitLabelPosition.RIGHT_TOP
        ll1.textSize = 10f

        val ll2 = LimitLine(5f, "Minimo registrado")
        ll2.lineWidth = 4f
        ll2.enableDashedLine(10f, 10f, 0f)
        ll2.labelPosition = LimitLabelPosition.RIGHT_BOTTOM
        ll2.textSize = 10f

        // draw limit lines behind data instead of on top

        // draw limit lines behind data instead of on top
        yAxis.setDrawLimitLinesBehindData(true)
//        xAxis.setDrawLimitLinesBehindData(true)

        // add limit lines

        // add limit lines
        yAxis.addLimitLine(ll1)
        yAxis.addLimitLine(ll2)
        /* xAxis.addLimitLine(llXAxis); */

        setData(30)

        // draw points over time

        // draw points over time
        chart.animateXY(2000, 2000)

        // get the legend (only possible after setting data)

        // get the legend (only possible after setting data)
        val l = chart.legend

        // draw legend entries as lines

        // draw legend entries as lines
        l.form = LegendForm.LINE
    }

    private fun setData(count: Int) {
        val values = ArrayList<Entry>()
        for (i in 0 until count) {
            val random = (0..60).random().toFloat()
            values.add(Entry(i.toFloat(), random, null))
        }
        val set1: LineDataSet
        if (chart.data != null && chart.data.dataSetCount > 0) {
            set1 = chart.data.getDataSetByIndex(0) as LineDataSet
            set1.values = values
            set1.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(values, "DataSet 1")

            // linea entre puntos
            set1.enableDashedLine(10f, 5f, 0f)

            // black lines and points
            set1.color = Color.BLACK
            set1.setCircleColor(Color.BLACK)
    
            // line thickness and point size
            set1.lineWidth = 1f
            set1.circleRadius = 6f

            // draw points as solid circles
            set1.setDrawCircleHole(false)

            // TODO: customize legend entry: ver que hace
//            set1.formLineWidth = 1f
//            set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
//            set1.formSize = 5f

            // Tamano valor puntos
            set1.valueTextSize = 9f

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f)

            // set the filled area
            set1.setDrawFilled(true)
            // TODO: revisr que hace esto
            set1.fillFormatter = IFillFormatter { dataSet, dataProvider -> chart.axisLeft.axisMinimum }

            // set color of filled area
            if (Utils.getSDKInt() >= 18) {
                // drawables only supported on api level 18 and above
                val drawable =
                    ContextCompat.getDrawable(this, R.drawable.fade_chart)
                set1.fillDrawable = drawable
            } else {
                set1.fillColor = Color.BLACK
            }
            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(set1) // add the data sets

            // create a data object with the data sets
            val data = LineData(dataSets)

            // set data
            chart.data = data
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnMensual -> {
                setData((0..100).random())
                chart.invalidate()
            }
            R.id.btnSemestral -> {
//                setData(180)
                chart.invalidate()
            }
            R.id.btnAnual -> {
                setData(365)
//                chart.invalidate()
            }
        }
    }
}

