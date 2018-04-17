import com.google.common.base.Function
import edu.uci.ics.jung.algorithms.layout.KKLayout
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.visualization.VisualizationImageServer
import edu.uci.ics.jung.visualization.decorators.EdgeShape
import edu.uci.ics.jung.visualization.layout.PersistentLayout
import edu.uci.ics.jung.visualization.layout.PersistentLayoutImpl
import edu.uci.ics.jung.visualization.renderers.Renderer
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.io.File
import java.time.LocalTime
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JLabel

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

private fun showJFrame(component: VisualizationImageServer<Number, Number>) {
    JFrame("Simple Graph View").let { f ->
        f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        f.contentPane.add(component)
        f.pack()
        f.isVisible = true
    }
}

fun Graph<Number, Number>.renderGraph(pairs: Map<Pair<Number, Number>, MutableList<Pair<Number, Number>>>) {
    //val pairs = vertices.findAllPairs()
    //val layout = FRLayout(this)

    val layout = KKLayout(this).apply {
        size = Dimension(1024, 1024)
    }
    val toSave = PersistentLayoutImpl(layout)
//    toSave.persist("SquarePlanar3x3Layout")
    VisualizationImageServer(layout, layout.size).run {
        pairs.forEach { key, combos ->
            combos.forEachIndexed { index, pair ->
                val jLabel = JLabel().apply {
                    font = Font("Serif", Font.BOLD, 35)
                    text = "Pair ${index + 1}: $key"
                    isOpaque = true
                    background = Color.WHITE
                    setBounds(30, 10, 275, 75)
                }

                renderContext.let { rc ->
                    background = Color.WHITE
                    renderer.vertexLabelRenderer.position = Renderer.VertexLabel.Position.CNTR
                    rc.vertexFontTransformer = Function { Font("Serif", Font.BOLD, 20) }
                    rc.setVertexLabelTransformer { vertex ->
                        vertex.toString()
                    }
                    rc.multiLayerTransformer.apply {
                        // getTransformer(Layer.LAYOUT).rotate(Math.PI/2, Point2D.Double(width/2.0, height/2.0))
                    }
                    rc.edgeShapeTransformer = EdgeShape.line(this@renderGraph)
                    rc.setVertexFillPaintTransformer { vertex ->
                        when (vertex) {
                            pair.first -> Color.RED
                            pair.second -> Color.YELLOW
                            else -> Color.LIGHT_GRAY
                        }
                    }
                    rc.setVertexStrokeTransformer { vertex ->
                        when (vertex == key.first || vertex == key.second) {
                            true -> BasicStroke(9f)
                            else -> when ((vertex == pair.first || vertex == pair.second) && pair.first == pair.second) {
                                true -> BasicStroke(10f)
                                else -> BasicStroke(2f)
                            }
                        }
                    }
                    rc.setVertexDrawPaintTransformer { vertex ->
                        when ((vertex == pair.first || vertex == pair.second) && pair.first == pair.second) {
                            true -> Color.YELLOW
                            else -> Color.BLACK
                        }
                    }
                    rc.setVertexShapeTransformer {
                        Ellipse2D.Double().apply {
                            width = 40.0
                            height = 40.0
                            x -= width / 2
                            y -= height / 2
                        }

                    }
                    add(jLabel)
                }
                //showJFrame(this)
                val image = getImage(Point2D.Double(graphLayout.size.width / 2.0, graphLayout.size.width / 2.0), Dimension(720, 720)) as BufferedImage
                saveImage(
                        image = image,
                        key = key,
                        pair = pair,
                        idx = index + 1)
                remove(jLabel)
            }
        }
    }
    println("Done!")
}

private fun <A, B> Pair<A, B>.isEmpty(): Boolean = this.first == -1 && this.second == -1

fun saveImage(image: BufferedImage, key: Pair<Number, Number>, pair: Pair<Number, Number>, idx: Int) {
    ImageIO.write((image), "png", File("images/$key$pair-$idx.png").also { it.mkdirs() })
    println("image: $pair was written to disk")
    //Thread.sleep(300)
}