import edu.uci.ics.jung.graph.DirectedSparseMultigraph
import edu.uci.ics.jung.graph.Hypergraph
import edu.uci.ics.jung.io.graphml.GraphMLReader2
import org.nield.kotlinstatistics.standardDeviation
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.util.*
import kotlin.system.measureTimeMillis

val threads_count = 8
val probability = 0.0
val Iterations = 640000 / threads_count
val inputGraph = HexagonalLattice.FLOWER_24
val graphs = loadGraphs(
        "flower_24",
        "flower_54",
        "flower_96",
        "flower_150",
        "HexagonGrid_19",
        "HexagonGrid_37",
        "HexagonGrid_61",
        "HexagonGrid_91",
        "HexagonGrid_127",
        "HexagonGrid_169",
        "HexagonGrid_217"
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

fun loadGraphs(vararg names: String): Map<Lattice, Hypergraph<Number, Number>> {
    return names.map<String, Pair<Lattice, Hypergraph<Number, Number>>> {
        val basePath = "E:\\Format\\Desktop\\Classes\\Research_walkers_MathNB\\Current\\Shapes"
        when (it.contains("flower")) {
            true -> HexagonalLattice.valueOf(it.toUpperCase()) to loadGraphML("$basePath\\Flower\\$it.graphml")
            else -> TriangularLattice.valueOf(it) to loadGraphML("$basePath\\Dual\\$it.graphml")
        }
    }.toMap()
}

fun display(list: List<Int>, time: Long) {
    val mean = list.average()
    val error = list.standardDeviation() / Math.sqrt(list.size.toDouble())
    listOf(("Calculating averages..."),
            ("Lattice: ${inputGraph.name}"),
            ("Iterations: $Iterations"),
            ("Walk Length: $mean"),
            ("Error: ${error * 100}%"),
            ("(+/-) ${error * 1.96}"),
            ("Samples:${list.size}"),
            ("Time: ${time / 1000.0} seconds"),
            ("-------------------------------")).joinToString(System.lineSeparator())
            .also { summary ->
                print(summary)
                File("E:\\Format\\Desktop\\Classes\\Research_walkers_MathNB\\Current\\Log\\${UUID.randomUUID()}.txt").printWriter().use {
                    it.write(summary)
                }
            }
}

private fun loadGraphML(s: String): Hypergraph<Number, Number> {
    val stream = FileInputStream(File(s))
    val reader0 = InputStreamReader(stream)
    val g = DirectedSparseMultigraph<String, String>()
    val reader = GraphMLReader2(reader0, graphFactory(), vertexFactory(), edgeFactory(), hyperEdgeFactory())
    return reader.readGraph()
}


private fun showMemory() {
    println("Total memory (bytes): " + Runtime.getRuntime().totalMemory())
    println("Free memory (bytes): " + Runtime.getRuntime().freeMemory())
    println("Max memory (bytes): " + Runtime.getRuntime().maxMemory())
}

fun findDeviation(nums: List<Int>, mean: Double): Double {
    var squareSum = 0.0
    nums.indices.forEach { i -> squareSum += Math.pow(nums[i] - mean, 2.0) }
    return Math.sqrt(squareSum / (nums.size - 1))
}

fun mean2(list: List<Int>): Double {
    var avg = 0.0
    var t = 1
    for (x in list) {
        avg += (x - avg) / t
        ++t
    }
    return avg
}


