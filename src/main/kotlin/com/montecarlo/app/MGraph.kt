package com.montecarlo.app

import com.montecarlo.lattice.Lattice
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath
import edu.uci.ics.jung.graph.Graph
import com.montecarlo.app.services.VirtualNode
import tornadofx.*
import java.util.*
import java.util.concurrent.*
import kotlin.math.absoluteValue

class MGraph(
        val Iterations: Int,
        val pb: Double,
        val graphInfo: Pair<Graph<Number, Number>, Lattice>
) : VirtualNode(graphInfo.second) {

    val graph = graphInfo.first
    var list = IntArray(Iterations)
    val trap = 15
    var steps = 0
    var walker1: Number = 0
    var walker2: Number = 0
    val positions: LinkedList<Number> = LinkedList()
    fun LinkedList<Number>.record(e: Number) {
        add(e)
        if (size > 4) remove()
    }

    fun goBackHistory(steps: Int): Number {
        //    println(this.toString())
        (steps).let { offset ->
            val index = ((positions.size - offset ).absoluteValue)
            return (positions[index].also {
                //positions.record(it)
            })
        }
    }

    fun run_sim(task: FXTask<*>? = null) {
        graphInfo.second.let { lattice ->
            (1..Iterations).forEachIndexed { index, it ->
                task?.updateTitle("Running Simulation!")
                task?.updateProgress(index.toLong(), Iterations.toLong())
                do {
                    walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]
                } while (walker1 == trap)
                while (walker1 != trap) {
                    walker1 = walk(walker1)
                    steps++
                }
                list[it - 1] = steps
                steps = 0
            }
        }
    }

    fun run_sierpinski() {
        val lattice = graphInfo.second
        (1..Iterations).forEach { iteration ->
            do {
                walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]

            } while (walker1 == lattice.centerPoint || walker1 == lattice.trap)

            if (!prob(pb)) {
                while ((walker1 != lattice.centerPoint) and (walker1 != lattice.trap)) {

                    val to = DijkstraDistance(graph).let { gr ->
                        val d1 = gr.getDistance(walker1, lattice.centerPoint).toInt()
                        val d2 = gr.getDistance(walker1, lattice.trap).toInt()
                        listOf(d1 to lattice.centerPoint,
                                d2 to lattice.trap).minBy { it.first }!!.second
                    }
                    // println("Walker at:$walker1")
                    walker1 = walk(walker1)
                    if (walker1 == 555) break
                    // println("Walked to:$walker1")
                }
            }
            // println("-----Exited")
            // println("-----Made steps: $steps")
            list[iteration - 1] = steps
            steps = 0
        }
    }

    fun run_sierpinski3D() {

        val lattice = graphInfo.second

        (1..Iterations).forEach { iteration ->

            do {
                walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]
            } while (walker1 == lattice.centerPoint || walker1 == lattice.trap || walker1 == lattice.trap2)

            val to = DijkstraDistance(graph).let { gr ->
                val d1 = gr.getDistance(walker1, lattice.centerPoint).toInt()
                val d2 = gr.getDistance(walker1, lattice.trap).toInt()
                val d3 = gr.getDistance(walker1, lattice.trap2).toInt()
                // if (d1 < d2 ) lattice.centerPoint else lattice.trap
                listOf(d1 to lattice.centerPoint,
                        d2 to lattice.trap,
                        d3 to lattice.trap2).minBy { it.first }!!.second
            }

            while ((walker1 != lattice.centerPoint) and (walker1 != lattice.trap) and (walker1 != lattice.trap2)) {
                // println("Walker at:$walker1")

                walker1 = walkShortest(walker1, to)

                // println("Walked to:$walker1")

            }
            //  println("-----Exited")
            // println("-----Made steps: $steps")
            list[iteration - 1] = steps
            steps = 0

        }
    }

    fun run_simTwoWalker(task: FXTask<*>? = null) {
        graphInfo.second.let { lattice ->
            (1..Iterations).forEach {
                task?.updateTitle("Running Simulation!")
                task?.updateProgress(it.toLong(), Iterations.toLong())
                do {
                    walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]
                    walker2 = graph.vertices.toList()[randomize(graph.vertexCount)]
                } while (walker1 == walker2)
                while (walker1 != walker2) {

                    val OLD_p = walker1
                    val OLD_p2 = walker2

                    walker1 = walk(walker1)
                    walker2 = walk(walker2)

                    steps++
                    if (walker1 == OLD_p2 && walker2 == OLD_p) {
                        break
                    }
                }
                list[it - 1] = steps
                steps = 0
            }
        }
    }

    fun run_simFast(task: FXTask<*>? = null) {

        graphInfo.second.let { lattice ->
            (1..Iterations).forEachIndexed { index, it ->
                task?.updateTitle("Running Simulation!")
                task?.updateProgress(index.toLong(), Iterations.toLong())
                do {
                    walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]
                } while (walker1 == lattice.trap)
                positions.clear()
                positions.record(walker1)

                while (walker1 != lattice.trap) {
                    steps++
                    if (!fun(): Boolean {
                                repeat(2) {
                                    walker1 = walk(walker1)
                                    when (walker1) {
                                        lattice.trap -> { return false }
                                    }

                                }
                                return true //means continue
                            }()) break
                }
                list[it - 1] = steps
                steps = 0
            }
        }
    }

    fun walk(vertex: Number): Number {
        steps++
        return graph.getNeighbors(vertex).toMutableList().let { neighbors ->
            neighbors.addVirtualSites(vertex).let { virtual ->
                virtual[randomize(virtual.size)]
                //neighbors[randomize(neighbors.size)]
            }
        }
    }


    fun walkShortest(from: Number, to: Number): Number {
        val edge = DijkstraShortestPath(graph).getPath(from, to).first()
        return graph.getEndpoints(edge).first { it != from }
    }
    fun prob(probabilityTrue: Double): Boolean = ThreadLocalRandom.current().nextDouble() >= 1.0 - probabilityTrue
    private fun randomize(grid_size: Int): Int = ThreadLocalRandom.current().nextInt(grid_size)
}
