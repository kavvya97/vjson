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

package vjson.pl.type.lang

import vjson.pl.ast.Type
import vjson.pl.type.*

abstract class CollectionType(private val alias: String, protected val elementType: TypeInstance) : TypeInstance, PluggableType {
  private val iteratorType = IteratorType()

  override fun initiate(ctx: TypeContext) {
    ctx.addType(Type(alias), this)
    ctx.addType(Type("$alias.Iterator"), iteratorType)
  }

  override fun field(ctx: TypeContext, name: String, accessFrom: TypeInstance?): Field? {
    val type = when (name) {
      "size" -> IntType
      "add" -> ctx.getFunctionDescriptorAsInstance(listOf(ParamInstance(elementType, 0)), BoolType, DummyMemoryAllocatorProvider)
      "remove" -> ctx.getFunctionDescriptorAsInstance(listOf(ParamInstance(elementType, 0)), BoolType, DummyMemoryAllocatorProvider)
      "iterator" -> iteratorType
      else -> null
    } ?: return null
    return Field(name, type, MemPos(0, 0), false)
  }

  private inner class IteratorType : TypeInstance {
    override fun field(ctx: TypeContext, name: String, accessFrom: TypeInstance?): Field? {
      val type = when (name) {
        "hasNext" -> BoolType
        "next" -> elementType
        else -> null
      } ?: return null
      return Field(name, type, MemPos(0, 0), false)
    }
  }
}
