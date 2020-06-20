package com.montecarlo.app.UI

import SierpinskiLattice
import TowerLattice
import TriangularLattice
import com.montecarlo.app.graphs
import com.montecarlo.app.inputGraph
import com.montecarlo.app.services.MarkovChains
import com.montecarlo.app.services.Simulation
import com.montecarlo.domain.BowtieLattice
import com.montecarlo.domain.Result
import com.montecarlo.domain.Walkers
import com.montecarlo.lattice.GraphWallType
import com.montecarlo.lattice.HoneycombLattice
import com.montecarlo.lattice.Lattice
import com.montecarlo.lattice.SquarePlanarLattice
import javafx.beans.Observable
import javafx.beans.value.ObservableValueBase
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.ToolBar
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.Style
import tornadofx.*
import tornadofx.controlsfx.toggleswitch
import java.util.concurrent.TimeUnit
import javax.swing.plaf.metal.MetalIconFactory.DARK
import kotlin.math.floor


var s: Lattice = HoneycombLattice.FLOWER_24
var walkers = Walkers.Two_Walkers
var ite = 2_000_000
val results = mutableListOf<Result>().asObservable()
var wallType = GraphWallType.LINEAR_HORIZONTAL
var lastIterations: Int = 1
var doPersist = true
var lastRecorderTime: Int = 0
val windowWidth = 800.0

class MonteCarloView : View() {

    override val root = BorderPane()

    val rightView: RightView by inject()
    val bottomView: BottomView by inject()
    val leftView: LeftView = LeftView()

    init {
        importStylesheet(Style.DARK.styleStylesheetURL)
        root.run {
            style {
                backgroundColor += c("#5A5A5A")
            }
            setPrefSize(windowWidth, 500.0)
            right = rightView.root
            bottom = bottomView.root
            left = leftView.root
        }
    }

}

class RightView : View() {
    override val root = BorderPane()

    init {
        with(root) {
            top = vbox {

                form {
                    label("Configuration") {
                        style {
                            fontSize = 20.px
                        }
                    }
                    fieldset {
                        field("Lattice Type") {
                            choicebox<Lattice> {
                                maxWidth = 180.0

                                items.addAll(TriangularLattice.values())
                                items.addAll(HoneycombLattice.values())
                                items.addAll(BowtieLattice.values())
                                items.addAll(SquarePlanarLattice.values())
                                items.addAll(SierpinskiLattice.values())
                                items.addAll(TowerLattice.values())
                                selectionModel.selectedItemProperty().addListener(
                                        ChangeListener<Lattice> { observable, oldValue, newValue ->
                                            s = newValue
                                        })
                            }
                        }
                        field("Number of Walkers") {
                            choicebox<Walkers> {
                                maxWidth = 180.0
                                items.addAll(Walkers.values())
                                selectionModel.selectedItemProperty().addListener(
                                        ChangeListener<Walkers> { observable, oldValue, newValue ->
                                            walkers = newValue
                                        })
                            }
                        }
                        field("WallType") {
                            choicebox<GraphWallType> {
                                maxWidth = 180.0
                                items.addAll(GraphWallType.values())
                                selectionModel.selectedItemProperty().addListener(
                                        ChangeListener<GraphWallType> { observable, oldValue, newValue ->
                                            wallType = newValue
                                        })
                            }
                        }
                        field("Persistence") {
                            togglebutton("ON") {
                                isSelected = true
                                action {
                                    doPersist = isSelected
                                    text = if (isSelected) "ON" else "OFF"
                                }


                            }
                        }

                    }
                }


                button("RUN") {
                    textFill = Color.RED
                    useMaxHeight = true
                    useMaxWidth = true

                    style {
                        backgroundColor += c("#C40000")
                    }
                    action {
                        runAsync {
                            // graphs[s]!!.vertices.forEach {
                       //     GraphWallType.values().forEach { wall ->
                                Simulation().start(
                                        input = s,
                                        iters = ite,
                                        wallType = wallType,
                                        fxTask = this
                                )
                          //  }
//                            }

                        }
                    }
                }
                button("Render Markov").apply {
                    useMaxHeight = true
                    useMaxWidth = true
                    action {
                        MarkovChains(s).getGraphViewer()
                    }
                }
            }
        }
    }
}

class TasksController : Controller() {
    val tasks: ObservableList<TaskStatus> = FXCollections.observableArrayList()
}

class BottomView : View() {
    val status: TaskStatus by inject()

    override val root = vbox {

        hbox {
            progressbar(status.progress) {
                prefWidth = windowWidth + 100
                style {


                }
            }
            label(status.message)
            visibleWhen { status.running }
        }
    }
}


class LeftView : View() {
    override val root = BorderPane()

    val label = label("0.0")

    val timeLeft = label("0.0")

    init {

        with(root) {
            top = hbox {
                tableview(results) {

                    readonlyColumn("Lattice", Result::lattice)
                    readonlyColumn("Walk Length", Result::walk_length).makeEditable()
                    readonlyColumn("Error", Result::error)
                    readonlyColumn("Range", Result::conf_interval)
                    readonlyColumn("Samples", Result::samples)
                    readonlyColumn("Time", Result::time)
                    readonlyColumn("s", Result::mortality)

                }
            }
            bottom = slider(10_000_000, 256_000_000) {
                //prefWidth = windowWidth+100
                value = 2000000.0
                majorTickUnit = 16_000_000.0
                minorTickCount = 0
                isSnapToTicks = true
                isShowTickMarks = true

                valueProperty().addListener(
                        ChangeListener { observable, oldValue, newValue ->
                            if (!isValueChanging) {
                                ite = value.toInt()
                                label.text = floor(newValue.toDouble()).toBigDecimal().toPlainString()
                                val est = (newValue.toDouble() / lastIterations.toDouble()) * (lastRecorderTime).toDouble()
                                timeLeft.text = "Estimated Time Cost: ${est.generateLabel()}"
                            }
                        }
                )
            }
            left = label
            right = timeLeft
        }
    }


    fun Double.generateLabel(): String {

        val minutes = TimeUnit.MILLISECONDS.toMinutes(this.toLong())

        return String.format("%d min, %d sec",
                minutes,
                TimeUnit.MILLISECONDS.toSeconds(this.toLong()) -
                        TimeUnit.MINUTES.toSeconds(minutes)
        )
    }

}