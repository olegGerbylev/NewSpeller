package new_speller.newSpeller

interface Speller {

    fun suggestReplacement(original: String?): String?
    fun suggestReplacements(original: String?): List<String?>?
}