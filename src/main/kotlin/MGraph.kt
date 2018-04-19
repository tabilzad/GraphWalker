import domain.HoneycombLattice
import domain.Lattice
import domain.TriangularLattice
import edu.uci.ics.jung.graph.Graph
import java.util.concurrent.*

class MGraph(val Iterations: Int, val pb: Double, val graphInfo: Pair<Graph<Number, Number>, Lattice>) {
    val graph = graphInfo.first
    var list = mutableListOf<Int>()
    var steps = 0
    var walker1: Number = 0
    var walker2: Number = 0

    fun run_sim() {
        graphInfo.second.let { lattice ->
            (1..Iterations).forEach {
                do {
                    walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]
                } while (walker1 == lattice.centerPoint)
                while (walker1 != lattice.centerPoint) {
                    walker1 = walk(lattice)(walker1)
                    steps++
                }
                list.add(steps)
                steps = 0
            }
        }
    }

    fun run_simTwoWalker() {
        graphInfo.second.let { lattice ->
            (1..Iterations).forEach {
                do {
                    walker1 = graph.vertices.toList()[randomize(graph.vertexCount)]
                    walker2 = graph.vertices.toList()[randomize(graph.vertexCount)]
                } while (walker1 == walker2)
                while (walker1 != walker2) {

                    val OLD_p = walker1
                    val OLD_p2 = walker2

                    walker1 = walk(lattice)(walker1)
                    walker2 = walk(lattice)(walker2)

                    steps++
                    if (walker1 == OLD_p2 && walker2 == OLD_p) {
                        break
                    }
                }
                list.add(steps)
                steps = 0
            }
        }
    }

    private fun walk(lattice: Lattice): (Number) -> Number {
        return when (lattice) {
            is TriangularLattice -> ::stepWithVirtualSitesForTriangle
            is HoneycombLattice -> ::stepWithVirtualSitesForHex
            else -> throw IllegalArgumentException("cant find this kind of Lattice")
        }

    }

    private fun stepWithVirtualSitesForHex(w: Number): Number {//works
        val neighbors = graph.getNeighbors(w).toMutableList()
        //neighbors.remove(w)
        return when {
            neighbors.size == 3 -> neighbors.toList()[randomize(neighbors.size)]
            neighbors.size == 2 -> neighbors.toList().plus(w)[randomize(neighbors.size)]
            else -> throw Exception("WRONG GRAPH/METHOD")
        }
    }

    private fun stepWithVirtualSitesForTriangle(w: Number): Number {
        val neighbors = graph.getNeighbors(w).toMutableList()
        //neighbors.remove(w)
        return when {
            neighbors.size == 6 -> neighbors.toList()[randomize(neighbors.size)]
            neighbors.size == 3 -> neighbors.toList().plus(listOf(w, w, w))[randomize(neighbors.size + 3)]
            neighbors.size == 4 -> neighbors.toList().plus(listOf(w, w))[randomize(neighbors.size + 2)]
            else -> throw Exception("you've done goofed")
        }
    }

    private fun step() {
        val neighbors = graph.getNeighbors(walker1)
        //neighbors.remove(walker1) //Comment out if u want Confining
        walker1 = neighbors.toList()[randomize(neighbors.size)]
    }

    fun prob(probabilityTrue: Double): Boolean = ThreadLocalRandom.current().nextDouble() >= 1.0 - probabilityTrue
    private fun randomize(grid_size: Int): Int = ThreadLocalRandom.current().nextInt(0, grid_size)
}
