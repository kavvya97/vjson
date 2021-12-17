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
import vjson.pl.inst.Instruction
import vjson.pl.inst.ThrowInst
import vjson.pl.type.ErrorType
import vjson.pl.type.NullType
import vjson.pl.type.StringType
import vjson.pl.type.TypeContext

data class ThrowStatement(val errMsgExpr: Expr? = null) : Statement() {
  private var ctx: TypeContext? = null

  override fun copy(): Statement {
    val ret = ThrowStatement(errMsgExpr?.copy())
    ret.lineCol = lineCol
    return ret
  }

  override fun checkAST(ctx: TypeContext) {
    this.ctx = ctx
    if (errMsgExpr != null) {
      val type = errMsgExpr.check(ctx)
      if (type !is StringType && type !is NullType && type !is ErrorType) {
        throw ParserException(
          "$this: throw statement expects string or null or error object, but got $errMsgExpr ($type)",
          lineCol
        )
      }
    }
  }

  override fun generateInstruction(): Instruction {
    return ThrowInst(errMsgExpr?.generateInstruction(), ctx!!.stackInfo(lineCol))
  }

  override fun functionTerminationCheck(): Boolean {
    return true
  }

  override fun toString(): String {
    return if (errMsgExpr == null) {
      "throw"
    } else {
      "throw: $errMsgExpr"
    }
  }
}
