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

package vjson.pl.inst

abstract class Instruction {
  abstract val stackInfo: StackInfo

  fun execute(ctx: ActionContext, values: ValueHolder) {
    if (ctx.returnImmediately) {
      return
    }
    try {
      execute0(ctx, values)
    } catch (e: InstructionException) {
      e.stackTrace.add(stackInfo)
      throw e
    } catch (e: Throwable) {
      val msg = e.message
      val ex = if (msg == null) {
        InstructionException(stackInfo, e)
      } else {
        InstructionException(msg, stackInfo, e)
      }
      throw ex
    }
  }

  protected abstract fun execute0(ctx: ActionContext, values: ValueHolder)
}