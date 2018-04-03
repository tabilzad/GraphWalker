import edu.uci.ics.jung.algorithms.layout.*
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.io.graphml.GraphMLReader2
import org.nield.kotlinstatistics.standardDeviation
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*
import edu.uci.ics.jung.visualization.VisualizationImageServer
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.geom.Point2D
import javax.swing.JFrame
import kotlin.system.measureTimeMillis


val threads_count = 8
val probability = 0.0
val Iterations = 64000000 / threads_count
val inputGraph = TriangularLattice.HexagonGrid_217
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
        "Square_3"
)

fun main(args: Array<String>) {
    //showMemory()
    val gList = Collections.synchronizedList(ArrayList<Int>())
    val time = measureTimeMillis {
        val threads = ArrayList<Thread>(threads_count)
        (1..threads_count).forEach { i ->
            threads.add(Thread(Runnable {
                val g = MGraph(Iterations, probability, graphs[inputGraph]!! to inputGraph)
                g.run_sim()
                gList.addAll(g.list)

            }))
            threads[i - 1].start()
        }
        (1..threads_count).forEach { i -> threads[i - 1].join() }
    }
    display(list = gList, time = time)
}

fun showJFrame(component: VisualizationImageServer<Number, Number>) {
    JFrame("Simple Graph View").let { f ->
        f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        f.contentPane.add(component)
        f.pack()
        f.isVisible = true
    }
}

fun saveImage(image: BufferedImage, index: Int) {
    ImageIO.write((image), "jpg", File("images/$index.jpg"))
    println("image: $index was written to disk")
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

fun display(list: List<Int>, time: Long) {
    val mean = list.average()
    val error = list.standardDeviation() / Math.sqrt(list.size.toDouble())
    listOf(("Calculating averages..."),
            ("Lattice: ${inputGraph.name}"),
            ("Samples: ${list.size}"),
            ("Walk Length: $mean"),
            ("Error: ${error * 100}%"),
            ("(+/-) ${error * 1.96}"),
            ("Time: ${time / 1000.0} seconds"),
            ("-------------------------------")).joinToString(System.lineSeparator())
            .also { summary ->
                print(summary)
                File("E:\\Format\\Desktop\\Classes\\Research_walkers_MathNB\\Current\\Log\\${UUID.randomUUID()}.txt").printWriter().use {
                    it.write(summary)
                }
            }
}

private fun loadGraphML(s: String): Graph<Number, Number> {
    val stream = FileInputStream(File(s))
    val reader = GraphMLReader2(InputStreamReader(stream), graphFactory(), vertexFactory(), edgeFactory(), hyperEdgeFactory())
    return reader.readGraph()
}


private fun showMemory() {
    println("Total memory (bytes): " + Runtime.getRuntime().totalMemory())
    println("Free memory (bytes): " + Runtime.getRuntime().freeMemory())
    println("Max memory (bytes): " + Runtime.getRuntime().maxMemory())
}


