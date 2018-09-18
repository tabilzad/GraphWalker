package services

import MGraph
import UI.results
import domain.Lattice
import domain.Result
import graphs
import org.nield.kotlinstatistics.standardDeviation
import probability
import threads_count
import java.sql.DriverManager
import java.time.LocalTime
import java.util.*
import kotlin.system.measureTimeMillis


class Simulation {

    fun start(input: Lattice, iters: Int) {
        println("Started simulation at: ${LocalTime.now()}")
        val gList = Collections.synchronizedList(ArrayList<Int>())
        val time = measureTimeMillis {
            val threads = ArrayList<Thread>(threads_count)
            (1..threads_count).forEach { i ->
                threads.add(Thread(Runnable {
                    val g = MGraph(iters / threads_count, probability, graphs[input]!! to input)
                    g.run_sierpinski()
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
        val standardDeviation = list.standardDeviation()
        val error = standardDeviation / Math.sqrt(list.size.toDouble())
        Result(
                lattice = lattice.name,
                samples = list.size.toString(),
                walk_length = mean.toString(),
                error = (error * 100).toBigDecimal().toPlainString().take(5),
                conf_interval = (error * 1.96).toBigDecimal().toPlainString().take(5),
                time = "${(time / 1000.0)}",
                mortality = probability
        ).let { result ->
            results.add(result)
            result.writeToDB()
            listOf(("Calculating averages..."),
                    ("Lattice: ${result.lattice}"),
                    ("Samples: ${result.samples}"),
                    ("Walk Length: ${result.walk_length}"),
                    ("Error: ${result.error}%"),
                    ("(+/-) ${result.conf_interval}"),
                    ("Time: ${result.time} seconds"),
                    ("-------------------------------\n")).joinToString(System.lineSeparator())
                    .also { summary -> print(summary) }
        }
    }

    private fun Result.writeToDB() {
        val url = "jdbc:sqlite:C:/sqlite/data.sql"
        DriverManager.getConnection(url).use { db ->
            db.prepareStatement("CREATE TABLE IF NOT EXISTS Data ( " +
                    " ID integer PRIMARY KEY AUTOINCREMENT," +
                    " lattice text NOT NULL," +
                    " samples integer NOT NULL," +
                    " walk_length real NOT NULL," +
                    " error text real NULL," +
                    " conf_interval real NOT NULL," +
                    " time real NOT NULL," +
                    " s real NOT NULL" +
                    " );").execute()
            db.prepareStatement("INSERT INTO Data(lattice, samples, walk_length, error, conf_interval, time, s)" +
                    " VALUES (\"$lattice\", \"$samples\", \"$walk_length\", \"$error\"," +
                    " \"$conf_interval\", \"$time\", \"$mortality\") ").executeUpdate()
        }
    }

    private fun showMemory() {
        println("Total memory (bytes): " + Runtime.getRuntime().totalMemory())
        println("Free memory (bytes): " + Runtime.getRuntime().freeMemory())
        println("Max memory (bytes): " + Runtime.getRuntime().maxMemory())
    }
}
