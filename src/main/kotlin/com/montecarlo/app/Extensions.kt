package com.montecarlo.app

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
import java.util.concurrent.ThreadLocalRandom
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
fun Pair<Number, Number>.inOrder() = if (first.toInt() < second.toInt()) this else second to first

fun <T, Y> Pair<T, Y>.isSame() = first == second

fun <E> List<E>.chooseRandom(): E? = if (size != 0) get(ThreadLocalRandom.current().nextInt(size)) else null

inline fun <T> MutableList<T>.mapInPlace(mutator: (T) -> T): List<T> {
    val iterate = this.listIterator()
    while (iterate.hasNext()) {
        val oldValue = iterate.next()
        val newValue = mutator(oldValue)
        if (newValue !== oldValue) {
            iterate.set(newValue)
        }
    }
    return iterate.asSequence().toList()
}

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

fun Pair<Number, Number>.getIndexIn(mat: List<IntArray>): Pair<Pair<Int, Int>, Pair<Int, Int>> {
    var coord: Pair<Int, Int> = -1 to -1
    var coord2: Pair<Int, Int> = -1 to -1

    mat.forEachIndexed { x, e ->
        e.forEachIndexed { y, e2 ->
            when (first == second) {
                true -> when (first) {
                    mat[x][y] -> {
                        coord = x to y
                        coord2 = x to y
                    }
                }
                else -> when {
                    (mat[x][y] == first) -> coord = x to y
                    (mat[x][y] == second) -> coord2 = x to y
                }
            }
        }
    }
    return coord to coord2
}

fun List<IntArray>.printMatrix() {
    for (i in 0 until size) {
        for (j in 0 until get(i).size) {
            print("" + this[i][j] + if (this[i][j].toString().length == 1) "  " else " ")
        }
        println()
    }
}

fun rotateMatrix(list: List<IntArray>, times: Int = 1): List<IntArray> {
    val mat = list.map { it.map { it }.toIntArray() }
    val N = list.size
    (0 until times).forEach {
        for (x in 0 until N / 2) {
            // Consider elements in group of 4 in
            // current square
            for (y in x until N - x - 1) {
                // store current cell in temp variable
                val temp = mat[x][y]

                // move values from right to top
                mat[x][y] = mat[y][N - 1 - x]

                // move values from bottom to right
                mat[y][N - 1 - x] = mat[N - 1 - x][N - 1 - y]

                // move values from left to bottom
                mat[N - 1 - x][N - 1 - y] = mat[N - 1 - y][x]

                // assign temp to left
                mat[N - 1 - y][x] = temp
            }
        }
    }
    return mat.toList()
}

fun flipMatrixV(list: List<IntArray>): List<IntArray> {
    val mat = list.map { it.map { it }.toIntArray() }
    val N = list.size
    for (i in 0 until N) {
        for (j in 0 until N / 2) {
            val temp = mat[i][j]
            mat[i][j] = mat[i][N - 1 - j]
            mat[i][N - 1 - j] = temp
        }
    }
    return mat.toList()
}

fun flipMatrixH(list: List<IntArray>): List<IntArray> {
    val mat = list.map { it.map { it }.toIntArray() }
    val N = list.size
    for (i in 0 until N / 2) {
        for (j in 0 until mat[i].size) {
            val temp = mat[i][j]
            mat[i][j] = mat[N - 1 - i][j]
            mat[N - 1 - i][j] = temp
        }
    }
    return mat.toList()
}


fun VisualizationImageServer<Number, Number>.saveImage(key: Pair<Number, Number>, pair: Pair<Number, Number>, idx: Int) {
    val image = getImage(Point2D.Double(graphLayout.size.width / 2.0, graphLayout.size.width / 2.0), Dimension(720, 720)) as BufferedImage
    ImageIO.write((image), "png", File("images/$key$pair-$idx.png").also { it.mkdirs() })
    println("image: $pair was written to disk")
    //Thread.sleep(300)
}