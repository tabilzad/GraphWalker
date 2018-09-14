package UI

import javafx.scene.layout.BorderPane
import services.Simulation
import domain.*
import domain.lattice.*
import javafx.scene.paint.Color
import services.MarkovChains
import tornadofx.*
import kotlin.math.floor


var s: Lattice = TriangularLattice.HexagonGrid_127
var ite = 1000
val results = mutableListOf<Result>().observable()

class MonteCarloView : View() {

    override val root = BorderPane()
    val rightView: RightView by inject()
    val bottomView: BottomView by inject()
    val leftView: LeftView = LeftView()

    init {
        root.run {
            setPrefSize(765.0, 450.0)
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
            top = choicebox<Lattice> {
                useMaxSize = true
                items.addAll(TriangularLattice.values())
                items.addAll(HoneycombLattice.values())
                items.addAll(SquarePlanarLattice.values())
                items.addAll(SierpinskiLattice.values())
                items.addAll(TowerLattice.values())
                selectionModel.selectedItemProperty().addListener(
                        ChangeListener<Lattice> { observable, oldValue, newValue ->
                            s = newValue
                        })
            }
            left = hbox {
                button("Simulate").apply {
                    textFill = Color.RED
                    action {
                        Simulation().start(s, ite)
                    }
                }
                button("Render Markov").apply {
                    textFill = Color.GREEN
                    action {
                        MarkovChains(s).calculateCombinations()
                    }
                }
            }
        }
    }

}

class BottomView : View() {
    override val root = BorderPane()

    init {
    }
}


class LeftView : View() {
    override val root = BorderPane()

    val label = label("0.0"){
             style {
                 size = Dimension(10.0, Dimension.LinearUnits.cm)
             }
    }
    init {
        with(root) {
            top = tableview(results) {
                readonlyColumn("Lattice", Result::lattice)
                readonlyColumn("Walk Length", Result::walk_length)
                readonlyColumn("Error", Result::error)
                readonlyColumn("Range", Result::conf_interval)
                readonlyColumn("Samples", Result::samples)
                readonlyColumn("Time", Result::time)
                columnResizePolicy = SmartResize.POLICY
                setPrefSize(650.0, 400.0)
            }
            bottom = slider(0, 100_000_000) {
                majorTickUnit = 1000000.0
                minorTickCount = 0
                isSnapToTicks = true
                isShowTickMarks = true
                valueProperty().addListener { o ->
                    ite = value.toInt()
                    label.text = floor(value).toBigDecimal().toPlainString()
                    println(value.toBigDecimal().toPlainString())
                }

            }
            left = label
        }
    }

}