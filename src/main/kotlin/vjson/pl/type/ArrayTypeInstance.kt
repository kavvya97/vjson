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

package vjson.pl.type

import vjson.pl.inst.ActionContext
import vjson.pl.inst.RuntimeMemoryTotal
import vjson.pl.inst.ValueHolder

class ArrayTypeInstance(private val elementType: TypeInstance) : TypeInstance {
  override fun memory(): RuntimeMemoryTotal {
    return RuntimeMemoryTotal(1, 0, 0, 0, 0)
  }

  private val intArrayLengthField = object : ExecutableField("length", IntType, MemPos(0, 0), false) {
    override fun execute(ctx: ActionContext, values: ValueHolder) {
      values.intValue = (values.refValue as IntArray).size
    }
  }
  private val longArrayLengthField = object : ExecutableField("length", IntType, MemPos(0, 0), false) {
    override fun execute(ctx: ActionContext, values: ValueHolder) {
      values.intValue = (values.refValue as LongArray).size
    }
  }
  private val floatArrayLengthField = object : ExecutableField("length", IntType, MemPos(0, 0), false) {
    override fun execute(ctx: ActionContext, values: ValueHolder) {
      values.intValue = (values.refValue as FloatArray).size
    }
  }
  private val doubleArrayLengthField = object : ExecutableField("length", IntType, MemPos(0, 0), false) {
    override fun execute(ctx: ActionContext, values: ValueHolder) {
      values.intValue = (values.refValue as DoubleArray).size
    }
  }
  private val boolArrayLengthField = object : ExecutableField("length", IntType, MemPos(0, 0), false) {
    override fun execute(ctx: ActionContext, values: ValueHolder) {
      values.intValue = (values.refValue as BooleanArray).size
    }
  }
  private val refArrayLengthField = object : ExecutableField("length", IntType, MemPos(0, 0), false) {
    override fun execute(ctx: ActionContext, values: ValueHolder) {
      values.intValue = (values.refValue as Array<*>).size
    }
  }

  override fun field(ctx: TypeContext, name: String, accessFrom: TypeInstance?): Field? {
    return when (name) {
      "length" -> when (elementType) {
        is IntType -> intArrayLengthField
        is LongType -> longArrayLengthField
        is FloatType -> floatArrayLengthField
        is DoubleType -> doubleArrayLengthField
        is BoolType -> boolArrayLengthField
        else -> refArrayLengthField
      }
      else -> null
    }
  }

  override fun elementType(ctx: TypeContext): TypeInstance {
    return elementType
  }
}
