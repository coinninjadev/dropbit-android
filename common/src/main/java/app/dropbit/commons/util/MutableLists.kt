package app.dropbit.commons.util

inline fun <reified T> MutableList<T>.removeRange(range: IntRange) {
    val fromIndex = range.start
    val toIndex = range.last
    if (fromIndex == toIndex) {
        return
    }

    if (fromIndex >= size) {
        throw IndexOutOfBoundsException("fromIndex $fromIndex >= size $size")
    }
    if (toIndex > size) {
        throw IndexOutOfBoundsException("toIndex $toIndex > size $size")
    }
    if (fromIndex > toIndex) {
        throw IndexOutOfBoundsException("fromIndex $fromIndex > toIndex $toIndex")
    }

    val filtered = filterIndexed { i, t -> i < fromIndex || i > toIndex }
    clear()
    addAll(filtered)
}