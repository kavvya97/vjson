/*
 * The MIT License
 *
 * Copyright 2021 wkgcass (https://github.com/wkgcass)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package vpreprocessor.token

sealed class Token

data class Plain(val text: String) : Token() {
  override fun toString(): String {
    return "Plain($text)"
  }
}

data class Macro(val text: String) : Token() {
  val category: MacroCategory =
    if (text == "ifdef" || text == "ifndef" || text == "else") MacroCategory.KEYWORD
    else if (text == "{{") MacroCategory.DOUBLE_BRACKET_LEFT
    else if (text == "}}") MacroCategory.DOUBLE_BRACKET_RIGHT
    else if (text == "{") MacroCategory.BRACKET_LEFT
    else if (text == "}") MacroCategory.BRACKET_RIGHT
    else MacroCategory.VAR // currently we only allow variables

  override fun toString(): String {
    return "Macro($text)"
  }
}

enum class MacroCategory {
  KEYWORD,
  VAR,
  BRACKET_LEFT,
  BRACKET_RIGHT,
  DOUBLE_BRACKET_LEFT,
  DOUBLE_BRACKET_RIGHT,
}

class EOFToken : Token() {
  override fun toString(): String {
    return "EOF"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null) return false
    if (this::class != other::class) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }
}
