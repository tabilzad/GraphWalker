package com.montecarlo.app.services

import com.montecarlo.app.*
import com.montecarlo.app.UI.*
import com.montecarlo.domain.Result
import com.montecarlo.domain.Walkers
import com.montecarlo.domain.Walkers.*
import kotlinx.coroutines.*
import org.nield.kotlinstatistics.descriptiveStatistics
import com.montecarlo.lattice.GraphWallType
import com.montecarlo.lattice.Lattice
import com.montecarlo.lattice.WallFactory
import tornadofx.Controller
import tornadofx.FXTask
import java.sql.DriverManager
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.measureTimeMillis

class Simulation : Controller() {
    fun start(
            input: Lattice,
            iters: Int,
            wallType: GraphWallType,
            fxTask: FXTask<*>
    ) {
        println("Started simulation at: ${LocalTime.now()}")

        val gList = IntArray(iters)
        var idx = 0
        val time = measureTimeMillis {
            runBlocking {
                (1..threads_count).map { i ->
                    GlobalScope.async(Dispatchers.IO) {
//                        val g = MGraph(
//                                Iterations = iters / threads_count,
//                                pb = probability,
//                                graphInfo = graphs[input]!! to input
//                        )
                        val g = MGraphWalled(
                                Iterations = iters / threads_count,
                                pb = probability,
                                graphInfo = graphs[input]!! to WallFactory.makeGraphWall(wallType)
                        )
                        when (walkers) {
                            One_Walker -> g.run_sim(fxTask)
                            Two_Walkers -> g.run_simTwoWalker(fxTask)
                            Two_WalkersNoTrap -> g.run_simTwoWalkerNoTrap(fxTask)
                            One_Walker_Fast -> g.run_simFast(fxTask)
                        }
                        g.list
                    }
                }.awaitAll().forEach {
                    it.copyInto(gList, destinationOffset = idx)
                    idx += it.size
                }
            }
        }

        display(
                list = gList,
                time = time,
                lattice = input,
                wallType = wallType
        )
    }

    private fun display(list: IntArray, time: Long, lattice: Lattice, wallType: GraphWallType) {
        lastRecorderTime = time.toInt()
        lastIterations = list.size
        val mean = list.average()
        val standardDeviation = list.descriptiveStatistics.standardDeviation
        val error = standardDeviation / Math.sqrt(list.size.toDouble())
        Result(
                lattice = wallType.name,
                samples = list.size.toString(),
                walk_length = mean.toString(),
                error = (error * 100).toBigDecimal().toPlainString().take(5),
                conf_interval = (error * 1.96).toBigDecimal().toPlainString().take(5),
                time = "${(time / 1000.0)}",
                mortality = probability
        ).let { result ->
            results.add(result)
            if (doPersist) {
                result.writeToDB()
            }
            listOf(("Calculating averages..."),
                    ("Lattice: ${wallType}_"),
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
        println("Written toDatabase")
        //TODO(this can be a textinput)
        val url = "jdbc:sqlite:C:/sqlite/TwoWalkers.sql"
        DriverManager.getConnection(url).use { db ->
            db.prepareStatement("CREATE TABLE IF NOT EXISTS Data ( " +
                    " ID integer PRIMARY KEY AUTOINCREMENT," +
                    " lattice text NOT NULL," +
                    " samples integer NOT NULL," +
                    " walk_length real NOT NULL," +
                    " error text real NULL," +
                    " conf_interval real NOT NULL," +
                    " time real NOT NULL," +
                    " s real NOT NULL," +
                    " timestamp text" +
                    " );").execute()
            db.prepareStatement("INSERT INTO Data(lattice, samples, walk_length, error, conf_interval, time, s, timestamp)" +
                    " VALUES (\"$lattice\", \"$samples\", \"$walk_length\", \"$error\"," +
                    " \"$conf_interval\", \"$time\", \"$mortality\", \"${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)}\")").executeUpdate()
        }
    }

    private fun showMemory() {
        println("Total memory (bytes): " + Runtime.getRuntime().totalMemory())
        println("Free memory (bytes): " + Runtime.getRuntime().freeMemory())
        println("Max memory (bytes): " + Runtime.getRuntime().maxMemory())
    }
}
