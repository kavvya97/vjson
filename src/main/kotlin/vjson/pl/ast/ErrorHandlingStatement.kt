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

package vjson.pl.ast

import vjson.ex.ParserException
import vjson.pl.inst.ErrorHandlingInstruction
import vjson.pl.inst.Instruction
import vjson.pl.type.ErrorType
import vjson.pl.type.MemPos
import vjson.pl.type.TypeContext
import vjson.pl.type.Variable

data class ErrorHandlingStatement(
  val tryCode: List<Statement>,
  val errorCode: List<Statement>,
  val elseCode: List<Statement>
) : Statement() {
  override fun copy(): ErrorHandlingStatement {
    val ret = ErrorHandlingStatement(tryCode.map { it.copy() }, errorCode.map { it.copy() }, elseCode.map { it.copy() })
    ret.lineCol = lineCol
    return ret
  }

  override fun checkAST(ctx: TypeContext) {
    ctx.checkStatements(tryCode)
    val errorCtx = TypeContext(ctx)
    errorCtx.addVariable(Variable("err", ErrorType, false, null, MemPos(0, 0)))
    errorCtx.checkStatements(errorCode)
    val elseCtx = TypeContext(ctx)
    elseCtx.checkStatements(elseCode)

    var tryCodeTerminate = false
    for (stmt in tryCode) {
      if (stmt.functionTerminationCheck()) {
        tryCodeTerminate = true
      }
    }
    if (tryCodeTerminate && elseCode.isNotEmpty()) {
      throw ParserException("$this: the code to be handled already terminates the function, no `else` should appear")
    }
  }

  @Suppress("DuplicatedCode")
  override fun functionTerminationCheck(): Boolean {
    var errorCodeTerminate = false
    for (stmt in errorCode) {
      if (stmt.functionTerminationCheck()) {
        errorCodeTerminate = true
        break
      }
    }
    if (!errorCodeTerminate) return false

    var elseCodeTerminate = false
    for (stmt in elseCode) {
      if (stmt.functionTerminationCheck()) {
        elseCodeTerminate = true
        break
      }
    }
    return elseCodeTerminate
  }

  override fun generateInstruction(): Instruction {
    val tryInst = tryCode.map { it.generateInstruction() }
    val errorCodeInst = errorCode.map { it.generateInstruction() }
    val elseCodeInst = elseCode.map { it.generateInstruction() }
    return ErrorHandlingInstruction(tryInst, errorCodeInst, elseCodeInst)
  }

  override fun toString(): String {
    return "ErrorHandling : $tryCode if: err != nil; then: $errorCode else: $elseCode"
  }
}
