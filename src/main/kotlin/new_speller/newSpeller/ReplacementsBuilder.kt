package new_speller.newSpeller

//import org.apache.commons.lang3.tuple.Pair
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ReplacementsBuilder {
    private val fromToPairs: MutableList<Pair<List<String>, List<String>>> = ArrayList()
    private var curFrom: List<String>? = null

    fun from(froms: List<String>): ReplacementsBuilder {
        assert(curFrom == null)
        curFrom = froms
        return this
    }

    fun to(tos: String): ReplacementsBuilder{
        return to(listOf(tos))
    }

    fun to(tos: List<String>): ReplacementsBuilder {
        assert(curFrom != null)
        fromToPairs.add(Pair(curFrom!!, tos))
        curFrom = null
        return this
    }

    fun build(): Map<String, List<String>> {
        val replacements: HashMap<String, ArrayList<String>> = HashMap()

        for (fromToPair in fromToPairs) {
            for (from in fromToPair.first) {
                replacements.computeIfAbsent(from) { k: String -> ArrayList() }.addAll(fromToPair.second)
            }
        }
        return replacements
    }
}