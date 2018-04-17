import domain.Lattice
import org.nield.kotlinstatistics.standardDeviation
import java.io.File
import java.time.LocalTime
import java.util.*

class Simulation {

    fun start(input: Lattice) {
        println("Started at: ${LocalTime.now()}")
        println("Started with chosen: ${input.name}")

        val g = graphs[input]!!

        val pairs = g.vertices
                .findAllPairs()
                .filterNot { it.first == it.second }
        print("rendering...")
        val hh = pairs.map { (a, b) ->
            (b to a) to mutableListOf<Pair<Number, Number>>().run {
                val neighbors = g.getNeighbors(a).toMutableList()
                when (neighbors.size) {
                    2 -> neighbors.addAll(listOf(a, a))
                    3 -> neighbors.add(a)
                }
                neighbors.forEach { pNeightbor ->
                    add(a to pNeightbor)
                }
                this
            }
        }
        println("")
        g.renderGraph(hh.associateBy({ it.first }, { it.second }))

/*val gList = Collections.synchronizedList(ArrayList<Int>())
val time = measureTimeMillis {
    val threads = ArrayList<Thread>(threads_count)
    (1..threads_count).forEach { i ->
        threads.add(Thread(Runnable {
            val g = MGraph(Iterations, probability, graphs[input]!! to input)
            g.run_simTwoWalker()
            gList.addAll(g.list)

        }))
        threads[i - 1].start()
    }
    (1..threads_count).forEach { i -> threads[i - 1].join() }
}
display(list = gList, time = time)*/
    }


    fun display(list: List<Int>, time: Long) {
        val mean = list.average()
        val error = list.standardDeviation() / Math.sqrt(list.size.toDouble())
        listOf(("Calculating averages..."),
                ("domain.Lattice: ${inputGraph.name}"),
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


    private fun showMemory() {
        println("Total memory (bytes): " + Runtime.getRuntime().totalMemory())
        println("Free memory (bytes): " + Runtime.getRuntime().freeMemory())
        println("Max memory (bytes): " + Runtime.getRuntime().maxMemory())
    }
}