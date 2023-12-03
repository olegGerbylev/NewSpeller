package new_speller.newSpeller.utils

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors

class StringCounter {


    private class Counter {
        var count: Long = 0
        fun inc(): Long {
            return ++count
        }

        fun add(amount: Long) {
            count += amount
        }
    }


    private val counter: MutableMap<String, Counter> = ConcurrentHashMap()
    private var totalCount: Long = 0

    fun add(text: String): Long {
        val count = counter.computeIfAbsent(text) { k: String? -> Counter() }.inc()
        totalCount++
        return count
    }

    fun add(text: String, count: Long) {
        counter.computeIfAbsent(text) { k: String? -> Counter() }.add(count)
        totalCount += count
    }

    operator fun get(text: String): Long {
        val c = counter[text]
        return c?.count ?: 0
    }

    fun addAll(texts: Collection<String>) {
        texts.forEach(Consumer { text: String -> this.add(text) })
    }

//    fun addAll(confusion: com.justai.nlp.utils.StringCounter) {
//        confusion.counter.forEach(BiConsumer { key: String, value: Counter ->
//            this.add(
//                key,
//                value.count
//            )
//        })
//    }

    fun getTotalCount(): Long {
        return totalCount
    }

    fun getUniqueCount(): Int {
        return counter.size
    }

    fun isEmpty(): Boolean {
        return totalCount == 0L
    }

    fun toOrderedList(): List<String> {
        return counter.entries.stream()
            .sorted(Comparator.comparingLong { (_, value): Map.Entry<String?, Counter> -> -value.count })
            .map<String>(Function<Map.Entry<String, Counter>, String> { (key, value) -> key })
            .collect(Collectors.toList<String>())
    }

    fun toFrequencyMap(): LinkedHashMap<String, Long> {
        val frequency = LinkedHashMap<String, Long>()
        counter.entries.stream()
            .sorted(Comparator.comparingLong { (_, value): Map.Entry<String?, Counter> -> -value.count })
            .forEach { (key, value): Map.Entry<String, Counter> ->
                frequency[key] = value.count
            }
        return frequency
    }

    override fun toString(): String {
        val list = counter.entries.stream()
            .sorted(Comparator.comparingLong { (_, value): Map.Entry<String?, Counter> -> -value.count })
            .map { (key, value): Map.Entry<String, Counter> -> key + "\t" + value.count }
            .collect(Collectors.toList())

        return list.joinToString(separator = "\n")
    }
}