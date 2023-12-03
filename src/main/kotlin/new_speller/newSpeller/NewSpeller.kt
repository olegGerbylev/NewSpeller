package new_speller.newSpeller

import com.google.common.base.Charsets
import morfologik.fsa.FSA
import morfologik.fsa.builders.FSABuilder
import morfologik.speller.Speller
import morfologik.stemming.Dictionary
import morfologik.stemming.DictionaryMetadataBuilder
import morfologik.stemming.EncoderType

import java.net.URL
import java.util.*
import java.util.stream.Collectors
import kotlin.Comparator
import kotlin.collections.HashMap


class NewSpeller(dictUrl: String) {
    init {
        try {
            val isStream = URL(dictUrl).openStream()

            val br = isStream.bufferedReader()

            br.readLine() // skip totalcount
            val wf = ArrayList<WordAndFrequency>()
            while (true) {
                val s = br.readLine() ?: break
                var splitStr= s.split("\t")
                wf.add(WordAndFrequency(splitStr[0], splitStr[1].toInt()))
            }
            br.close()
            init(wf)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
    constructor(wordAndFrequencies: List<WordAndFrequency>) : this("") {
        init(wordAndFrequencies)
    }

    private lateinit var spellersByEditDistance: Array<Speller>
    private lateinit var editDistanceThresholds: IntArray
    private lateinit var frequences: Map<String, Int>
    private lateinit var distance: DamerauLevenshteinDistance

    private fun init(wordAndFrequencies: List<WordAndFrequency>) {
        val fsa = prepareFSA(wordAndFrequencies)
        val replacements = fillReplacements()
        val dict = buildDictionary(fsa, replacements)
        fillSpellersByDistance(dict)
        fillFrequenciesMap(wordAndFrequencies)
        distance = DamerauLevenshteinDistance(1, 1, 1, 1)
    }

    private fun fillSpellersByDistance(dict: Dictionary) {
        spellersByEditDistance = arrayOf(Speller(dict, 1), Speller(dict, 2))
        editDistanceThresholds = intArrayOf(3, 5)
    }

    private fun buildDictionary(fsa: FSA, replacements: Map<String, List<String>>): Dictionary {
        val md = DictionaryMetadataBuilder()
            .encoder(EncoderType.SUFFIX)
            .supportRunOnWords(true)
            .encoding(Charsets.UTF_8)
            .withReplacementPairs(replacements)
            .separator('\t')
            .build()
        return Dictionary(fsa, md)
    }

    private fun fillReplacements(): Map<String, List<String>> {
        val replacements = ReplacementsBuilder()
            .from(listOf("йо", "ио", "йе", "ие")).to("и")
            .from(listOf("о", "ы", "я")).to("а")
            .from(listOf("е", "ё", "э")).to("и")
            .from(listOf("ю")).to("у")
            .from(listOf("б")).to("п")
            .from(listOf("з")).to("с")
            .from(listOf("д")).to("т")
            .from(listOf("в")).to("ф")
            .from(listOf("г")).to("к")
            .from(listOf("тс", "дс")).to("ц")
            .from(listOf("нн")).to("н")
            .from(listOf("лл")).to("л")
            .build()
        return replacements
    }

    private fun prepareFSA(wordAndFrequencies: List<WordAndFrequency>): FSA {
        val lines = wordAndFrequencies.stream()
            .sorted(Comparator.comparing { s -> s.word })
            .map { s -> s.word.toByteArray(Charsets.UTF_8) }
            .collect(Collectors.toList())

        return FSABuilder.build(lines)
    }

    private fun fillFrequenciesMap(wordAndFrequencies: List<WordAndFrequency>) {
        frequences = HashMap()
        for (wf in wordAndFrequencies) {
            (frequences as HashMap<String, Int>)[wf.word] = wf.frequency
        }
    }

    fun suggestReplacement(original: String): String? {
        for (i in editDistanceThresholds.size - 1 downTo 0) {
            if (original.length > editDistanceThresholds[i]) {
                return suggestReplacementWithSpeller(original, spellersByEditDistance[i])
            }
        }
        return null
    }

    fun suggestReplacements(original: String): List<String>? {
        for (i in editDistanceThresholds.size - 1 downTo 0) {
            if (original.length > editDistanceThresholds[i]) {
                return suggestReplacementsWithSpeller(original, spellersByEditDistance[i])
            }
        }
        return null
    }

    private fun suggestReplacementsWithSpeller(original: String, speller: Speller): List<String>? {
        var replacements = speller.findReplacements(original)

        if (replacements.isNullOrEmpty()) {
            return null
        }

        val hasTheSame = replacements.any { r ->
            normalize(r) == normalize(original)
        }

        if (hasTheSame) {
            return null
        }

        replacements = sortByDamerauLevenshteinAndFreq(original, replacements) as ArrayList<String>
        return replacements
    }

    private fun suggestReplacementWithSpeller(original: String, speller: Speller): String? {
        val replacements = suggestReplacementsWithSpeller(original, speller)
        return replacements?.get(0)
    }

    private fun normalize(word: String): String {
        return word.lowercase().replace("ё", "е")
    }

    private fun sortByDamerauLevenshteinAndFreq(source: String, replacements: List<String>): List<String> {
        val wdfs = replacements.map { word ->
            val dlDist = distance.execute(source, word)
            val freq = if (word.contains(" ")) {
                word.lowercase(Locale.getDefault()).split(" ").filter { it.length > 2 }.map { frequences[it] ?: 0 }.maxOrNull() ?: 0
            } else {
                frequences[word.lowercase()] ?: 0
            }
            WordDistFreq(word, dlDist, freq)
        }
        return wdfs.sortedBy { it.frequency }.sortedBy { it.dist }.map { it.word }
    }
}

fun main(){
    var speller = NewSpeller("file:///home/oleg/Documents/test1.txt")

    println(speller.suggestReplacements("превет"))
}

