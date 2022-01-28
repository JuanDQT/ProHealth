package com.quispe.coagutest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.anychart.graphics.vector.Stroke
import com.quispe.coagutest.database.room.MyDatabase
import com.quispe.coagutest.database.room.RoomControlDataSource
import com.quispe.coagutest.databinding.ActivityGraphBinding
import com.quispe.coagutest.repository.ControlRepository
import com.quispe.coagutest.ui.GraphViewModel

class GraphActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGraphBinding
    private lateinit var viewModel: GraphViewModel
    private lateinit var controlRepository: ControlRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildDependencies()
        viewModel = buildViewModel()
        setUpUI()
        subscribeUI()
    }

    private fun buildDependencies() {
        val database = MyDatabase.getDatabase(this)
        val controlLocal = RoomControlDataSource(database)
        controlRepository = ControlRepository(controlLocal)
    }

    private fun buildViewModel(): GraphViewModel {
        val factory = GraphViewModelFactory(controlRepository)
        return ViewModelProvider(this, factory).get(GraphViewModel::class.java)
    }

    private fun setUpUI() {
        binding = ActivityGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun subscribeUI() {
        viewModel.controlList.observe(this) { list ->
            prepareGraphSettings(list)
        }
    }

    private fun prepareGraphSettings(seriesData: MutableList<DataEntry>) {
        binding.acvGrafica.setProgressBar(findViewById(R.id.progress_bar))

        val cartesian = AnyChart.line()

        cartesian.animation(true)

        cartesian.padding(10.0, 20.0, 5.0, 20.0)

        cartesian.crosshair().enabled(true)
        cartesian.crosshair()
            .yLabel(true)
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
        //hacer la linea mas gruesa...
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

}
