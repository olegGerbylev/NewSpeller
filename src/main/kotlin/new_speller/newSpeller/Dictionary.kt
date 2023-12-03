package new_speller.newSpeller

import org.apache.commons.io.IOUtils
import new_speller.newSpeller.utils.StringCounter
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.regex.Pattern
import java.util.stream.Collectors

class Dictionary {




    private var words: HashMap<String, Int>? = null
    private var totalCount = 0

    fun Dictionary(stream: InputStream?) {
        load(stream)
    }

    fun load(stream: InputStream?) {
        val lines: MutableList<String> = IOUtils.readLines(
                stream,
                Charset.forName("UTF-8")
            )
        if (lines.isEmpty()) {
            words = HashMap()
            return
        }
        totalCount = lines.removeAt(0).toInt()
        words = HashMap()
        lines.forEach(Consumer<String> { l: String ->
            val parts: List<String> = l.split("\t")
            if (parts.size != 2) {
                throw RuntimeException("Illegal format for dictionary entry $l")
            }
            try {
                val count = Integer.valueOf(parts[1])
                words!![parts[0].trim { it <= ' ' }] = count
                totalCount += count
            } catch (e: Exception) {
                throw RuntimeException("Cannot parse line '$l' in dictionary")
            }
        })
    }

    fun hasWord(word: String): Boolean {
        return words!!.containsKey(word)
    }

    fun getFrequency(word: String): Int {
        return words!!.getOrDefault(word, 0)
    }
    companion object{
        private val wordRegexp = Pattern.compile("[а-яА-Я]+")
        fun buildDictionary(sourceFilename: String?, outputFilename: String) {
            val text: String = File(sourceFilename).readText(Charsets.UTF_8)
            var words: List<String> = extractWords(text)
            words =
                words.stream().map(Function<String, String> { normalize(it) }).collect(Collectors.toList())
            val counter = StringCounter()
            words.forEach(
                Consumer { w: String ->
                    counter.add(w)
                    if (counter.getTotalCount() % 10000 === 0L) {
                        System.out.println(counter.toOrderedList().size)
                    }
                }
            )
            saveCorpusData(outputFilename, counter)
        }

        private fun saveCorpusData(filename: String, words: StringCounter) {
            val os = File(filename).outputStream().bufferedWriter()

            os.write(words.getTotalCount().toString())
            os.newLine()

            words.toFrequencyMap().forEach { w, c ->
                os.write("$w\t$c")
                os.newLine()
            }

            os.close()
        }

        fun extractWords(text: String): List<String> {
            val ret: MutableList<String> = ArrayList()
            val matcher = wordRegexp.matcher(text.lowercase(Locale.getDefault()))
            while (matcher.find()) {
                ret.add(matcher.group())
            }
            return ret
        }
        fun normalize(word: String): String? {
            return word.lowercase(Locale.getDefault()).replace("ё".toRegex(), "е")
        }

    }
}



fun main() {
    Dictionary.buildDictionary("/home/oleg/Documents/test.txt", "/home/oleg/Documents/test1.txt")
//        buildDictionary("C:\\workspace\\beeline-logs\\data.tsv", "C:\\workspace\\beeline-logs\\dict-large.txt");
}