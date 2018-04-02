/**
 * Created by FERMAT on 4/2/2018.
 */


fun <T> MutableCollection<T>.findAllPairs(): List<Pair<T, T>> = mutableListOf<Pair<T, T>>().let {
    this.forEachIndexed { index, a ->
        (index + 0 until this.size).mapTo(it) { it -> a to this.toList()[it] }
    }
    it
}
