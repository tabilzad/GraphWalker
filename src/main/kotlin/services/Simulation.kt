package services

import domain.Lattice
import graphs
import org.nield.kotlinstatistics.standardDeviation
import probability
import threads_count
import java.io.File
import java.time.LocalTime
import java.util.*
import kotlin.system.measureTimeMillis
import MGraph
import UI.results
import domain.Result

class Simulation {

    fun start(input: Lattice, iters: Int) {
        println("Started simulation at: ${LocalTime.now()}")
        val gList = Collections.synchronizedList(ArrayList<Int>())
        val time = measureTimeMillis {
            val threads = ArrayList<Thread>(threads_count)
            (1..threads_count).forEach { i ->
                threads.add(Thread(Runnable {
                    val g = MGraph(iters / threads_count, probability, graphs[input]!! to input)
                    g.run_sierpinski3D()
                    gList.addAll(g.list)

                }))
                threads[i - 1].start()
            }
            (1..threads_count).forEach { i -> threads[i - 1].join() }
        }
        display(
                list = gList,
                time = time,
                lattice = input
        )
    }

    private fun display(list: List<Int>, time: Long, lattice: Lattice) {
        val mean = list.average()
        val error = list.standardDeviation() / Math.sqrt(list.size.toDouble())
        Result(
                lattice = lattice.name,
                samples = list.size.toString(),
                walk_length = mean.toString(),
                error = (error * 100).toString(),
                range = (error * 1.96).toString(),
                time = (time / 1000.0).toString()
        ).let { result ->
            results.add(result)
            listOf(("Calculating averages..."),
                    ("Lattice: ${result.lattice}"),
                    ("Samples: ${result.samples}"),
                    ("Walk Length: ${result.walk_length}"),
                    ("Error: ${result.error}%"),
                    ("(+/-) ${result.range}"),
                    ("Time: ${result.time} seconds"),
                    ("-------------------------------\n")).joinToString(System.lineSeparator())
                    .also { summary ->
                        print(summary)
                        File("E:\\Format\\Desktop\\Classes\\Research_walkers_MathNB\\Current\\Log\\${UUID.randomUUID()}.txt").printWriter().use {
                            it.write(summary)
                        }
                    }
        }
    }

    private fun showMemory() {
        println("Total memory (bytes): " + Runtime.getRuntime().totalMemory())
        println("Free memory (bytes): " + Runtime.getRuntime().freeMemory())
        println("Max memory (bytes): " + Runtime.getRuntime().maxMemory())
    }
}
