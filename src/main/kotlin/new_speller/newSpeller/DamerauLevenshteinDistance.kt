package new_speller.newSpeller

import java.util.HashMap

class DamerauLevenshteinDistance(deleteCost: Int, insertCost: Int, replaceCost: Int, swapCost: Int) {
    private val deleteCost: Int
    private val insertCost: Int
    private val replaceCost: Int
    private val swapCost: Int

    init {
        if (2 * swapCost < insertCost + deleteCost) {
            throw IllegalArgumentException("Unsupported cost assignment")
        }
        this.deleteCost = deleteCost
        this.insertCost = insertCost
        this.replaceCost = replaceCost
        this.swapCost = swapCost
    }

    fun execute(source: String, target: String): Int {
        if (source.length == 0) {
            return target.length * insertCost
        }
        if (target.length == 0) {
            return source.length * deleteCost
        }
        val table = Array(source.length) { IntArray(target.length) }
        val sourceIndexByCharacter: MutableMap<Char, Int> = HashMap()
        if (source[0] != target[0]) {
            table[0][0] = Math.min(replaceCost, deleteCost + insertCost)
        }
        sourceIndexByCharacter[source[0]] = 0
        for (i in 1 until source.length) {
            val deleteDistance = table[i - 1][0] + deleteCost
            val insertDistance = (i + 1) * deleteCost + insertCost
            val matchDistance = i * deleteCost +
                    if (source[i] == target[0]) 0 else replaceCost
            table[i][0] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance)
        }
        for (j in 1 until target.length) {
            val deleteDistance = (j + 1) * insertCost + deleteCost
            val insertDistance = table[0][j - 1] + insertCost
            val matchDistance = j * insertCost +
                    if (source[0] == target[j]) 0 else replaceCost
            table[0][j] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance)
        }
        for (i in 1 until source.length) {
            var maxSourceLetterMatchIndex = if (source[i] == target[0]) 0 else -1
            for (j in 1 until target.length) {
                val candidateSwapIndex = sourceIndexByCharacter[target[j]]
                var jSwap = maxSourceLetterMatchIndex
                val deleteDistance = table[i - 1][j] + deleteCost
                val insertDistance = table[i][j - 1] + insertCost
                var matchDistance = table[i - 1][j - 1]
                matchDistance += if (source[i] != target[j]) replaceCost else {
                    maxSourceLetterMatchIndex = j
                    0
                }
                var swapDistance: Int
                swapDistance = if (candidateSwapIndex != null && jSwap != -1) {
                    val iSwap = candidateSwapIndex
                    val preSwapCost: Int = if (iSwap == 0 && jSwap == 0) {
                        0
                    } else {
                        table[Math.max(0, iSwap - 1)][Math.max(0, jSwap - 1)]
                    }
                    preSwapCost + (i - iSwap - 1) * deleteCost +
                            (j - jSwap - 1) * insertCost + swapCost
                } else {
                    Int.MAX_VALUE
                }
                table[i][j] = Math.min(Math.min(Math.min(deleteDistance, insertDistance), matchDistance), swapDistance)
            }
            sourceIndexByCharacter[source[i]] = i
        }
        return table[source.length - 1][target.length - 1]
    }
}