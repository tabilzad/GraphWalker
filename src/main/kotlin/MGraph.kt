import domain.Lattice
import edu.uci.ics.jung.graph.Graph
import services.VirtualNode
import java.util.concurrent.*

class MGraph(
        val Iterations: Int,
        val pb: Double,
        val graphInfo: Pair<Graph<Number, Number>, Lattice>
) : VirtualNode(graphInfo.second) {
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
                    walker1 = walk(walker1)
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

                    walker1 = walk(walker1)
                    walker2 = walk(walker2)

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


    fun walk(vertex: Number): Number {
        return graph.getNeighbors(vertex).toMutableList().let { neighbors ->
            neighbors.addVirtualSites(vertex).let { virtual ->
                virtual[randomize(virtual.size)]
            }
        }
    }


    fun prob(probabilityTrue: Double): Boolean = ThreadLocalRandom.current().nextDouble() >= 1.0 - probabilityTrue
    private fun randomize(grid_size: Int): Int = ThreadLocalRandom.current().nextInt(0, grid_size)
}
