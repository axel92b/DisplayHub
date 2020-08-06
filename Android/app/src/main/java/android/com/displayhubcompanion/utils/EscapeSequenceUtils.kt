package android.com.displayhubcompanion.utils

class EscapeSequenceUtils {
    companion object {

        fun getEscapeSequence(vararg strings: String) : String{
            return strings.joinToString(prefix = "^", postfix = "\n", separator = "|")
        }

        fun splitEscapeSequence(sequence: String) : List<String> {
            return sequence.split("|","^","\n").filter { it.isNotEmpty() }
        }

    }
}