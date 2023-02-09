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
package vjson.util

import vjson.JSON
import vjson.simple.*
import vjson.util.functional.Consumer_
import vjson.util.functional.Supplier_
import kotlin.reflect.KClass

class ObjectBuilder {
  private val map: MutableList<SimpleObjectEntry<JSON.Instance<*>>> = ArrayList()

  constructor()
  constructor(f: ObjectBuilder.() -> Unit) {
    f(this)
  }

  constructor(o: JSON.Object) {
    for (entry in o.entryList()) {
      putInst(entry.key, entry.value)
    }
  }

  fun putInst(key: String, inst: JSON.Instance<*>): ObjectBuilder {
    if (key == "@type") { // always add @type to the most front
      map.add(0, SimpleObjectEntry(key, inst))
    } else {
      map.add(SimpleObjectEntry(key, inst))
    }
    return this
  }

  fun putNullableInst(key: String, isNull: Boolean, instSupplier: () -> JSON.Instance<*>): ObjectBuilder {
    if (isNull) {
      return put(key, null)
    } else {
      return putInst(key, instSupplier())
    }
  }

  fun putNullableInst(key: String, isNull: Boolean, instSupplier: Supplier_<JSON.Instance<*>>): ObjectBuilder {
    return putNullableInst(key, isNull, instSupplier as () -> JSON.Instance<*>)
  }

  fun put(key: String, bool: Boolean): ObjectBuilder {
    return putInst(key, SimpleBool(bool))
  }

  fun put(key: String, integer: Int): ObjectBuilder {
    return putInst(key, SimpleInteger(integer))
  }

  fun put(key: String, longV: Long): ObjectBuilder {
    return putInst(key, SimpleLong(longV))
  }

  fun put(key: String, doubleV: Double): ObjectBuilder {
    return putInst(key, SimpleDouble(doubleV))
  }

  fun put(key: String, num: Double, exponent: Int): ObjectBuilder {
    return putInst(key, SimpleExp(num, exponent))
  }

  fun put(key: String, string: String?): ObjectBuilder {
    if (string == null) {
      return putInst(key, SimpleNull())
    } else {
      return putInst(key, SimpleString(string))
    }
  }

  fun putObject(key: String, func: ObjectBuilder.() -> Unit): ObjectBuilder {
    val builder = ObjectBuilder()
    func(builder)
    return putInst(key, builder.build())
  }

  fun putObject(key: String, func: Consumer_<ObjectBuilder>): ObjectBuilder {
    return putObject(key, func as (ObjectBuilder) -> Unit)
  }

  fun putArray(key: String, func: ArrayBuilder.() -> Unit): ObjectBuilder {
    val builder = ArrayBuilder()
    func(builder)
    return putInst(key, builder.build())
  }

  fun putArray(key: String, func: Consumer_<ArrayBuilder>): ObjectBuilder {
    return putArray(key, func as (ArrayBuilder) -> Unit)
  }

  fun type(type: String): ObjectBuilder {
    return putInst("@type", SimpleString(type))
  }

  fun type(aClass: KClass<*>): ObjectBuilder {
    return type(aClass./* #ifdef KOTLIN_JS {{ simpleName }} else {{ */qualifiedName/* }} */!!)
  }

  fun build(): JSON.Object {
    return object : SimpleObject(map, TrustedFlag.FLAG) {}
  }
}
