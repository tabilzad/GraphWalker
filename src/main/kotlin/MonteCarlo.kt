import UI.MyApp
import domain.*
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.io.graphml.GraphMLReader2
import javafx.application.Application
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader


val threads_count = 8
val probability = 0.0
val Iterations = 128/*_000_000*/ / threads_count
val inputGraph = HoneycombLattice.FLOWER_24
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
        "Square_5",
        "sierpinski_6",
        "sierpinski_15",
        "sierpinski_42",
        "sierpinski_123"
)

fun main(args: Array<String>) {
    //showMemory()
    Application.launch(MyApp::class.java, *args)
}

fun loadGraphs(vararg names: String): Map<Lattice, Graph<Number, Number>> {
    return names.map<String, Pair<Lattice, Graph<Number, Number>>> {
        val basePath = "E:\\Format\\Desktop\\Classes\\Research_walkers_MathNB\\Current\\Shapes"
        when {
            it.contains("flower") -> HoneycombLattice.valueOf(it.toUpperCase()) to loadGraphML("$basePath\\Flower\\$it.graphml")
            it.contains("Grid") -> TriangularLattice.valueOf(it) to loadGraphML("$basePath\\Dual\\$it.graphml")
            it.contains("sierpinski") -> SierpinskiLattice.from(it) to loadGraphML("$basePath\\Fractal\\$it.graphml")
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