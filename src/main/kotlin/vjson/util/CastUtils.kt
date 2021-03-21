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

import kotlin.reflect.KClass

/**
 * this object is only used to bypass the limitations of jacoco<br>
 * some kotlin generated code (e.g. null check) will never be reached
 */
// #ifdef COVERAGE {{@lombok.Generated}}
@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
object CastUtils {
  inline fun <R> cast(t: Any?): R {
    return t as R
  }

  inline fun <T : Any> check(t: Any?, type: KClass<T>): Boolean {
    return type.isInstance(t)
  }

  inline fun <T : Any> typeIsExpectedAnd(t: Any?, type: KClass<T>, check: (T) -> Boolean): Boolean {
    return t != null && type.isInstance(t) && check(t as T)
  }

  inline fun <T : Any> typeIsNotExpectedOr(t: Any?, type: KClass<T>, check: (T) -> Boolean): Boolean {
    return t == null || !type.isInstance(t) || check(t as T)
  }
}
