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
import vjson.pl.inst.*
import vjson.pl.type.*

data class FunctionInvocation(
  val target: Expr,
  val args: List<Expr>
) : Expr() {
  override fun copy(): FunctionInvocation {
    val ret = FunctionInvocation(target.copy(), args.map { it.copy() })
    ret.lineCol = lineCol
    return ret
  }

  override fun check(ctx: TypeContext): TypeInstance {
    this.ctx = ctx
    val targetType = target.check(ctx)
    val func = targetType.functionDescriptor(ctx)
      ?: throw ParserException("$this: unable to invoke $target, which is not a functional object", lineCol)
    if (func.params.size != args.size) {
      throw ParserException(
        "$this: unable to invoke $target with $args, arguments count (${args.size}) parameters count (${func.params.size}) mismatch",
        lineCol
      )
    }
    for (idx in args.indices) {
      val argType = args[idx].check(ctx)
      val paramType = func.params[idx]
      if (!TypeUtils.assignableFrom(paramType.type, argType)) {
        throw ParserException(
          "$this: unable to invoke $target with $args, args[$idx] $argType does not match params[$idx] $paramType",
          lineCol
        )
      }
    }
    return func.returnType
  }

  override fun typeInstance(): TypeInstance {
    return target.typeInstance().functionDescriptor(ctx)!!.returnType
  }

  override fun generateInstruction(): Instruction {
    return buildFunctionInvocationInstruction(ctx, this, args.map { it.generateInstruction() })
  }

  override fun toString(): String {
    return "($target: $args)"
  }

  companion object {
    fun buildFunctionInvocationInstruction(ctx: TypeContext, func: FunctionInvocation, args: List<Instruction>): Instruction {
      val funcDesc = func.target.typeInstance().functionDescriptor(ctx)!!
      val funcInst = func.target.generateInstruction()
      return object : Instruction() {
        override val stackInfo: StackInfo = ctx.stackInfo(func.lineCol)
        override fun execute0(ctx: ActionContext, values: ValueHolder) {
          if (funcInst is FunctionInstance) {
            funcInst.ctxBuilder = { buildContext(ctx, it, values, funcDesc, args) }
            funcInst.execute(ctx, values)
          } else {
            funcInst.execute(ctx, values)
            val funcValue = values.refValue as Instruction
            val newCtx = buildContext(ctx, ctx, values, funcDesc, args)
            funcValue.execute(newCtx, values)
          }
        }
      }
    }

    private fun buildContext(
      callerCtx: ActionContext,
      ctx: ActionContext,
      values: ValueHolder,
      funcDesc: FunctionDescriptor,
      args: List<Instruction>
    ): ActionContext {
      val newCtx = ActionContext(funcDesc.mem.memoryAllocator().getTotal(), ctx)
      val newMem = newCtx.getCurrentMem()

      for (i in args.indices) {
        args[i].execute(callerCtx, values)
        val param = funcDesc.params[i]
        when (param.type) {
          is IntType -> newMem.setInt(param.memIndex, values.intValue)
          is LongType -> newMem.setLong(param.memIndex, values.longValue)
          is FloatType -> newMem.setFloat(param.memIndex, values.floatValue)
          is DoubleType -> newMem.setDouble(param.memIndex, values.doubleValue)
          is BoolType -> newMem.setBool(param.memIndex, values.boolValue)
          else -> newMem.setRef(param.memIndex, values.refValue)
        }
      }

      return newCtx
    }
  }
}
