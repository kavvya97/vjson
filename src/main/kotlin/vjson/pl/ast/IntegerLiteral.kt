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

import vjson.JSON
import vjson.pl.inst.Instruction
import vjson.pl.inst.LiteralInt
import vjson.pl.inst.LiteralLong
import vjson.pl.type.IntType
import vjson.pl.type.LongType
import vjson.pl.type.TypeContext
import vjson.pl.type.TypeInstance

data class IntegerLiteral(val n: JSON.Number<*>) : Expr() {
  var typeHint: Type? = null

  override fun copy(): IntegerLiteral {
    val ret = IntegerLiteral(n)
    ret.typeHint = typeHint?.copy()
    ret.lineCol = lineCol
    return ret
  }

  override fun check(ctx: TypeContext): TypeInstance {
    this.ctx = ctx
    if (typeHint != null && typeHint!!.check(ctx) is LongType) return LongType
    return if (n is JSON.Long) LongType else IntType
  }

  override fun typeInstance(): TypeInstance {
    if (typeHint != null && typeHint!!.typeInstance() is LongType) return LongType
    return if (n is JSON.Long) LongType else IntType
  }

  override fun generateInstruction(): Instruction {
    return if (typeInstance() == LongType) {
      LiteralLong(n.toJavaObject().toLong(), ctx.stackInfo(lineCol))
    } else {
      LiteralInt(n.toJavaObject().toInt(), ctx.stackInfo(lineCol))
    }
  }

  override fun toString(indent: Int): String {
    return "" + n.stringify()
  }

  override fun toString(): String {
    return toString(0)
  }
}
