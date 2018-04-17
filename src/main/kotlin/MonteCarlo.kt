import UI.MonteCarloView
import UI.MyApp
import domain.HexagonalLattice
import domain.Lattice
import domain.SquarePlanarLattice
import domain.TriangularLattice
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.io.graphml.GraphMLReader2
import edu.uci.ics.jung.visualization.VisualizationImageServer
import javafx.application.Application
import org.nield.kotlinstatistics.standardDeviation
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.time.LocalTime
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JFrame
import kotlin.system.measureTimeMillis


val threads_count = 8
val probability = 0.0
val Iterations = 128_000_000 / threads_count
val inputGraph = HexagonalLattice.FLOWER_24
val graphs = loadGraphs(
        "flower_24",
        "flower_54",
        "flower_96",
        "flower_150",
        "flower_216",
        "HexagonGrid_19",
        "HexagonGrid_37",
        "HexagonGrid_61",
        "HexagonGrid_91",
        "HexagonGrid_127",
        "HexagonGrid_169",
        "HexagonGrid_217",
        "Square_3",
        "Square_5"
)

fun main(args: Array<String>) {
    //showMemory()
    Application.launch(MyApp::class.java, *args)

}

fun loadGraphs(vararg names: String): Map<Lattice, Graph<Number, Number>> {
    return names.map<String, Pair<Lattice, Graph<Number, Number>>> {
        val basePath = "E:\\Format\\Desktop\\Classes\\Research_walkers_MathNB\\Current\\Shapes"
        when {
            it.contains("flower") -> HexagonalLattice.valueOf(it.toUpperCase()) to loadGraphML("$basePath\\Flower\\$it.graphml")
            it.contains("Grid") -> TriangularLattice.valueOf(it) to loadGraphML("$basePath\\Dual\\$it.graphml")
            else -> SquarePlanarLattice.valueOf(it) to loadGraphML("$basePath\\Square\\$it.graphml")
        }
    }.toMap()
}

private fun loadGraphML(s: String): Graph<Number, Number> {
    val stream = FileInputStream(File(s))
    val reader = GraphMLReader2(InputStreamReader(stream), graphFactory(), vertexFactory(), edgeFactory(), hyperEdgeFactory())
    return reader.readGraph()
}


//Both variants of the representation are completely different!