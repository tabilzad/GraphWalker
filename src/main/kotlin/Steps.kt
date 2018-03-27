import edu.uci.ics.jung.graph.Hypergraph
import java.util.concurrent.ThreadLocalRandom

/**
* Created by FERMAT on 3/26/2018.
*/
sealed class Steps(val graph: Hypergraph<Number, Number>, var current: Number) {

    private fun stepWithVirtualSitesForHex() {//works
        val neighbors = graph.getNeighbors(current)
        neighbors.remove(current)
        current = when {
            neighbors.size == 3 -> neighbors.toList()[randomize(neighbors.size)]
            neighbors.size == 2 -> neighbors.toList().plus(current)[randomize(neighbors.size)]
            else -> throw Exception("WRONG GRAPH/METHOD")
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

    private fun randomize(grid_size: Int): Int = ThreadLocalRandom.current().nextInt(0, grid_size)

}
