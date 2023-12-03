package new_speller.newSpeller

data class WordAndFrequency(val word: String, val frequency: Int) {
    constructor(word: String) : this(word, 0)
}
