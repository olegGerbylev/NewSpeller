package new_speller

import com.mlp.sdk.MlpExecutionContext
import com.mlp.sdk.MlpExecutionContext.Companion.systemContext
import com.mlp.sdk.MlpPredictServiceBase
import com.mlp.sdk.MlpServiceSDK
import new_speller.newSpeller.NewSpeller

data class newSpellerActionRequest(
    val word: String
)

class newSpellerService(
    override val context: MlpExecutionContext
) : MlpPredictServiceBase<newSpellerActionRequest, String>(REQUEST_EXAMPLE, RESPONSE_EXAMPLE) {
    private val speller: NewSpeller
    init {
        speller = NewSpeller("file://../../resources/test1.txt")
    }

    override fun predict(req: newSpellerActionRequest): String {
        return speller.suggestReplacements(req.word).toString()
    }

    companion object {
        val REQUEST_EXAMPLE = newSpellerActionRequest("перевет")
        val RESPONSE_EXAMPLE = "[привет]"
    }
}

fun main() {
    val actionSDK = MlpServiceSDK({ newSpellerService(systemContext) })

    actionSDK.start()
    actionSDK.blockUntilShutdown()
}
