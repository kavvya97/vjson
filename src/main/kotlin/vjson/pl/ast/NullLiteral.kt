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

import vjson.pl.inst.Instruction
import vjson.pl.inst.LiteralNull
import vjson.pl.type.NullType
import vjson.pl.type.TypeContext
import vjson.pl.type.TypeInstance

data class NullLiteral(val type: Type? = null) : Expr() {
  override fun copy(): NullLiteral {
    val ret = NullLiteral(type?.copy())
    ret.lineCol = lineCol
    return ret
  }

  override fun check(ctx: TypeContext): TypeInstance {
    this.ctx = ctx
    if (type == null) {
      return NullType
    }
    return type.check(ctx)
  }

  override fun typeInstance(): TypeInstance {
    if (type == null) {
      return NullType
    }
    return type.typeInstance()
  }

  override fun generateInstruction(): Instruction {
    return LiteralNull(ctx.stackInfo(lineCol))
  }

  override fun toString(indent: Int): String {
    return if (type == null) "null" else "{null: $type}"
  }

  override fun toString(): String {
    return toString(0)
  }
}
