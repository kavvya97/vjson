package vjson.pl

import vjson.CharStream
import vjson.JSON
import vjson.Stringifier
import vjson.parser.ParserOptions
import vjson.parser.ParserUtils
import vjson.util.PrintableChars

class ScriptifyContext(private val indent: Int) {
  private var currentIndent: Int = 0
  var isTopLevel = true
    private set

  fun unsetTopLevel() {
    isTopLevel = false
  }

  fun increaseIndent() {
    currentIndent += indent
  }

  fun decreaseIndent() {
    if (currentIndent - indent < 0) {
      throw IllegalStateException()
    }
    currentIndent -= indent
  }

  fun appendIndent(builder: StringBuilder) {
    for (i in 0 until currentIndent) {
      builder.append(" ")
    }
  }

  companion object {
    fun scriptifyString(s: String): String {
      if (stringNoQuotes(s)) return s
      return JSON.String.stringify(s, stringifyStringOptions)
    }

    private val stringifyStringOptions = Stringifier.StringOptions.Builder().apply {
      printableChar = PrintableChars.EveryCharExceptKnownUnprintable
    }.build()

    private fun stringNoQuotes(s: String): Boolean {
      if (s.isBlank()) return false
      if (s.trim() != s) return false
      return checkStringNoQuotesWithParser(s)
    }

    private fun checkStringNoQuotesWithParser(s: String): Boolean {
      val json = try {
        ParserUtils.buildFrom(CharStream.from(s), ParserOptions.allFeatures())
      } catch (ignore: Throwable) {
        return false
      }
      if (json !is JSON.String) return false
      return json.toJavaObject() == s
    }
  }
}
