import edu.uci.ics.jung.algorithms.layout.FRLayout
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.visualization.VisualizationImageServer
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import java.awt.Color
import java.awt.Dimension
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D
import java.awt.image.BufferedImage

/**
 * Created by FERMAT on 4/2/2018.
 */


fun <T> MutableCollection<T>.findAllPairs(): List<Pair<T, T>> = mutableListOf<Pair<T, T>>().let {
    this.forEachIndexed { index, a ->
        (index + 0 until this.size).mapTo(it) { it -> a to this.toList()[it] }
    }
    it
}

fun findDeviation(nums: List<Int>, mean: Double): Double {
    var squareSum = 0.0
    nums.indices.forEach { i -> squareSum += Math.pow(nums[i] - mean, 2.0) }
    return Math.sqrt(squareSum / (nums.size - 1))
}

fun mean2(list: List<Int>): Double {
    var avg = 0.0
    var t = 1
    for (x in list) {
        avg += (x - avg) / t
        ++t
    }
    return avg
}

fun Graph<Number, Number>.renderGraph() {
    val pairs = vertices.findAllPairs()
    val layout = FRLayout(this)
    layout.size = Dimension(640, 640) // sets the initial size of the space
    VisualizationImageServer(layout, layout.size).run {
        pairs.forEachIndexed { index, pair ->
            renderContext.let {
                it.multiLayerTransformer.apply {
                    // getTransformer(Layer.LAYOUT).rotate(Math.PI/2, Point2D.Double(width/2.0, height/2.0))
                }
                it.edgeShapeTransformer = EdgeShape.line(this@renderGraph)
                it.setVertexFillPaintTransformer { vertex ->
                    when (pair.first == vertex || pair.second == vertex) {
                        true -> Color.BLUE
                        false -> Color.CYAN
                    }
                }
                it.setVertexShapeTransformer {
                    Ellipse2D.Double().apply {
                        width = 20.0
                        height = 20.0
                        x -= width / 2
                        y -= height / 2
                    }

                }
            }
            preferredSize = Dimension(640, 640) //Sets the viewing area size
            //showJFrame(this)
            val image = getImage(Point2D.Double(graphLayout.size.width / 2.0, graphLayout.size.height / 2.0), graphLayout.size) as BufferedImage
            saveImage(image, index)
        }
    }
}
