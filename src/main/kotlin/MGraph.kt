import edu.uci.ics.jung.graph.Hypergraph
import java.util.concurrent.*
import kotlin.system.measureTimeMillis

class MGraph(val Iterations: Int, val pb: Double, val graph: Hypergraph<Number, Number>) {
    var list = mutableListOf<Int>()
    var steps = 0
    private var current: Number = 0
    fun run_sim() {
        (1..Iterations).forEach {
            do {
                current = graph.vertices.toList()[randomize(graph.vertexCount)]
            } while (current == 41)
            while (current != 41) {
                //stepWithVirtualSitesForTriangle()
                stepWithVirtualSitesForHex()
                steps++
            }
            list.add(steps)
            steps = 0
        }
    }

    private fun stepWithVirtualSitesForHex() {//works
        val neighbors = graph.getNeighbors(current)
        neighbors.remove(current)
        current = when {
            neighbors.size == 3 -> neighbors.toList()[randomize(neighbors.size)]
            neighbors.size == 2 -> neighbors.toList().plus(current)[randomize(neighbors.size)]
            else -> throw Exception("dfssdf")
        }
    }

    private fun stepWithVirtualSitesForTriangle() {
        val neighbors = graph.getNeighbors(current)
        neighbors.remove(current)
        current = when {
            neighbors.size == 6 -> neighbors.toList()[randomize(neighbors.size)]
            neighbors.size == 3 -> neighbors.toList().plus(listOf(current, current, current))[randomize(neighbors.size + 3)]
            neighbors.size == 4 -> neighbors.toList().plus(listOf(current, current))[randomize(neighbors.size + 2)]
            else -> throw Exception("you've done goofed")
        }
    }

    private fun step() {
        val neighbors = graph.getNeighbors(current)
        //neighbors.remove(current) //Comment out if u want Confining
        current = neighbors.toList()[randomize(neighbors.size)]
    }

    fun prob(probabilityTrue: Double): Boolean = ThreadLocalRandom.current().nextDouble() >= 1.0 - probabilityTrue
    private fun randomize(grid_size: Int): Int = ThreadLocalRandom.current().nextInt(0, grid_size)
}
