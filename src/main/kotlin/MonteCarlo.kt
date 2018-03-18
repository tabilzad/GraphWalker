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
val Iterations = 64000 / threads_count
val graphPath = "E:\\Format\\Desktop\\Classes\\Research_walkers_MathNB\\Current\\Shapes\\Flower\\flower_96.graphml"
//val graphPath = "E:\\Format\\Desktop\\Classes\\Research_walkers_MathNB\\Current\\Shapes\\Dual\\triangularHexagonGrid_91.graphml"
val g = loadGraphML(graphPath)

fun main(args: Array<String>) {
    val time = measureTimeMillis {
        val gList = Collections.synchronizedList(ArrayList<Int>())
        val threads = ArrayList<Thread>(threads_count)
        (1..threads_count).forEach { i ->

            threads.add(Thread(Runnable {
                val g = MGraph(Iterations, probability, g)
                g.run_sim()
                gList.addAll(g.list)
            }))
            threads[i - 1].start()
        }

        (1..threads_count).forEach { i -> threads[i - 1].join() }
        display(list = gList)
    }
    println("Time: " + time / 1000.0
            + " seconds")
}

fun display(list: List<Int>) {
    println(StringBuilder().run {
        append("Calculating averages...")
        append(System.lineSeparator())
        val mean = list.average()
        append("Walk Length: " + mean)
        append(System.lineSeparator())
        val error = list.standardDeviation() / Math.sqrt(list.size.toDouble())
        println("Error: " + error * 100 + "%")
        append("(+/-) " + error * 1.96)
        append(System.lineSeparator())
        append("Samples:" + list.size)
        append(System.lineSeparator())
        append("-------------------------------")
        toString()
    })
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


