package UI

import javafx.scene.layout.BorderPane
import services.Simulation
import domain.*
import javafx.scene.paint.Color
import services.MarkovChains
import tornadofx.*


var s: Lattice = TriangularLattice.HexagonGrid_127

class MonteCarloView : View() {

    override val root = BorderPane()
    val data = Result("sd", "sd", "3", "3", "d", "3")
    val rightView: RightView by inject()
    val bottomView: BottomView by inject()
    val leftView: LeftView = LeftView(listOf(data))

    init {
        root.run {
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
                items.addAll(TriangularLattice.values())
                items.addAll(HoneycombLattice.values())
                items.addAll(SquarePlanarLattice.values())
                selectionModel.selectedItemProperty().addListener(
                        ChangeListener<Lattice> { observable, oldValue, newValue ->
                            s = newValue
                        })
            }
            left = hbox {
                button("Simulate").apply {
                    textFill = Color.RED
                    action {
                        Simulation().start(s)
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


class LeftView(data: List<Result>) : View() {
    override val root = BorderPane()

    init {
        with(root) {
            top = tableview(data.observable()) {
                readonlyColumn("Lattice", Result::lattice)
                readonlyColumn("Walk Length", Result::walk_length)
                readonlyColumn("Error", Result::error)
                readonlyColumn("Range", Result::range)
                readonlyColumn("Samples", Result::samples)
                readonlyColumn("Time", Result::time)
            }
        }
    }

}