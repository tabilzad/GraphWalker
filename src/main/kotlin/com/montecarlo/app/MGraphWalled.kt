package com.montecarlo.app

import com.montecarlo.app.services.VirtualNode
import com.montecarlo.domain.Particle2D
import com.montecarlo.lattice.GraphWall
import com.montecarlo.lattice.SquarePlanarLattice
import edu.uci.ics.jung.graph.Graph
import tornadofx.FXTask
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.absoluteValue

class MGraphWalled(
        val Iterations: Int,
        val pb: Double,
        val graphInfo: Pair<Graph<Number, Number>, GraphWall>
) : VirtualNode(SquarePlanarLattice.Square_6) {

    val graph = graphInfo.first
    var list = IntArray(Iterations)
    var steps = 0
    var walker1: Number = 0
    var walker2: Number = 0
    val positions: LinkedList<Number> = LinkedList()
    val positions2: LinkedList<Number> = LinkedList()

    fun  LinkedList<Number>.goBackHistory(steps: Int): Number {
        //    println(this.toString())
        (steps).let { offset ->
            val index = ((size - offset ).absoluteValue)
            return (this[index].also {
                 //positions.record(it)
            })
        }
    }

    override fun toString(): String {
        return "History:=${positions
                .map { it }
                .joinToString(", => ", "(", ")")}"
    }

    fun LinkedList<Number>.record(e: Number) {
        add(e)
        if (size > 4) remove()
    }


    fun run_sim(task: FXTask<*>? = null) {

        graphInfo.second.let { lattice ->
            (1..Iterations).forEachIndexed { index, it ->
                task?.updateTitle("Running Simulation!")
                task?.updateProgress(index.toLong(), Iterations.toLong())
                do {
                    walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]
                } while (walker1 == lattice.trap || lattice.points.any { walker1 == it })

                while (walker1 != lattice.trap) {

                    val walker_old = walker1

                    walker1 = walk(walker1)
                    steps++
                    if (lattice.points.any { it == walker1 }) {
                        walker1 = walker_old
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
                } while (walker1 == lattice.trap || lattice.points.any { walker1 == it })
                positions.clear()
                positions.record(walker1)

                while (walker1 != lattice.trap) {
                    steps++
                    if (!fun(): Boolean {
                                repeat(2) {
                                    walker1 = walk(walker1)
                                    when {
                                        walker1 == lattice.trap -> { return false }
                                        lattice.points.any { it == walker1 } -> {
                                            if (it == 0) {
                                                walker1 = positions.goBackHistory(1)
                                                //return true
                                            } else {
                                                walker1 = positions.goBackHistory(2)
                                                // println("Now at(${p.x}, ${p.y})")
                                            }
                                        }
                                        else -> return true

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

    fun run_simTwoWalker(task: FXTask<*>? = null) {
        graphInfo.second.let { lattice ->
            (0 until Iterations).forEachIndexed { index, iteration ->
                task?.updateTitle("Running Simulation!")
                task?.updateProgress(index.toLong(), Iterations.toLong())
                //val walls = listOf<Number>()
                val walls = lattice.points
                val trap = lattice.trap

                walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]
                walker2 = graph.vertices.toList()[randomize(graph.vertexCount)]


                while (walls.any { it == walker1 || it == walker2 } ||
                        walker1 == trap ||
                        walker2 == trap) {
                    when {
                        walker1 == trap -> walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]
                        walker2 == trap -> walker2 = graph.vertices.toList()[randomize(graph.vertexCount)]
                        walls.any { it == walker1 } -> walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]
                        walls.any { it == walker2 } -> walker2 = graph.vertices.toList()[randomize(graph.vertexCount)]
                    }
                }

                while (true) {

                    val walker_old = walker1
                    val walker2_old = walker2

                    walker1 = walk(walker1)
                    walker2 = walk(walker2)

                    steps++
                    if (walls.any { it == walker1 }) {
                        walker1 = walker_old
                    }

                    if (walls.any { it == walker2 }) {
                        walker2 = walker2_old
                    }

                    if (walker1 == trap || walker2 == trap) break


                }

                list[iteration] = steps
                steps = 0
            }
        }
    }

    fun run_simTwoWalkerNoTrap(task: FXTask<*>? = null) {
        graphInfo.second.let { lattice ->
            (0 until Iterations).forEachIndexed { index, iteration ->
                task?.updateTitle("Running Simulation!")
                task?.updateProgress(index.toLong(), Iterations.toLong())
                val walls = lattice.points

                walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]
                walker2 = graph.vertices.toList()[randomize(graph.vertexCount)]


                while (walls.any { it == walker1 || it == walker2 } ||
                        walker1 == walker2
                ) {
                    when {
                        walker2 == walker1 -> walker2 = graph.vertices.toList()[randomize(graph.vertexCount)]
                        walls.any { it == walker1 } -> walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]
                        walls.any { it == walker2 } -> walker2 = graph.vertices.toList()[randomize(graph.vertexCount)]
                    }
                }

                while (walker1 != walker2) {

                    val walker_old = walker1
                    val walker2_old = walker2

                    walker1 = walk(walker1)
                    walker2 = walk(walker2)

                    steps++
                    if (walls.any { it == walker1 }) {
                        walker1 = walker_old
                    }

                    if (walls.any { it == walker2 }) {
                        walker2 = walker2_old
                    }


                    if (walker1 == walker2_old && walker2 == walker_old) break

                }

                list[iteration] = steps
                steps = 0
            }
        }
    }

    fun walk(vertex: Number): Number {
        positions.record(vertex)
        return graph.getNeighbors(vertex).toMutableList().let { neighbors ->
            neighbors.addVirtualSites(vertex).let { virtual ->
                virtual[randomize(virtual.size)]
                //neighbors[randomize(neighbors.size)]
            }
        }
    }

    fun prob(probabilityTrue: Double): Boolean = ThreadLocalRandom.current().nextDouble() >= 1.0 - probabilityTrue
    private fun randomize(grid_size: Int): Int = ThreadLocalRandom.current().nextInt(grid_size)
}
