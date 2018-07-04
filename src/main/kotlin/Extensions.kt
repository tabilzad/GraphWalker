import edu.uci.ics.jung.visualization.VisualizationImageServer
import org.apache.poi.util.Units
import org.apache.poi.xwpf.usermodel.BreakType
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.awt.Dimension
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import javax.imageio.ImageIO


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

fun <T, Y> Pair<T, Y>.flip() = second to first

private fun <A, B> Pair<A, B>.isEmpty(): Boolean = this.first == -1 && this.second == -1


fun makeWordDocument(images: List<BufferedImage>) {

    val d = XWPFDocument()
    val p = d.createParagraph()
    val r = p.createRun()

    d.document.body.addNewSectPr().addNewPgMar().apply {
        left = BigInteger.valueOf(360)
        right = BigInteger.valueOf(360)
        top = BigInteger.valueOf(360)
        bottom = BigInteger.valueOf(360)
    }

    images.forEachIndexed { index, bufferedImage ->
        println("Writing to doc: $index")
        ByteArrayOutputStream().let { os ->
            ImageIO.write(bufferedImage, "png", os)
            val stream = ByteArrayInputStream(os.toByteArray())
            when (index % 17 == 0) {
                false -> {
                    r.addPicture(stream, XWPFDocument.PICTURE_TYPE_PNG, "$index", Units.toEMU(140.0), Units.toEMU(140.0))
                }
                true -> {
                    if (index != 0) r.addBreak(BreakType.PAGE)
                    r.addPicture(stream, XWPFDocument.PICTURE_TYPE_PNG, "$index", Units.toEMU(450.0), Units.toEMU(450.0))
                    r.addBreak(BreakType.PAGE)
                }
            }
            stream.close()
        }
    }
    val out = FileOutputStream("images/doc1.docx")
    d.write(out)
    out.flush()
    out.close()
}

fun VisualizationImageServer<Number, Number>.saveImage(key: Pair<Number, Number>, pair: Pair<Number, Number>, idx: Int) {
    val image = getImage(Point2D.Double(graphLayout.size.width / 2.0, graphLayout.size.width / 2.0), Dimension(720, 720)) as BufferedImage
    ImageIO.write((image), "png", File("images/$key$pair-$idx.png").also { it.mkdirs() })
    println("image: $pair was written to disk")
    //Thread.sleep(300)
}