package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

sealed class Either<out L, out R> {
    data class Left<out L>(val left: L) : Either<L, Nothing>()
    data class Right<out R>(val right: R) : Either<Nothing, R>()
}

// Either accept the left argument or the right argument type. Left is tried first.
class EitherArg<L, R>(val left: ArgumentType<L>, val right: ArgumentType<R>, name: String = "") : ArgumentType<Either<L, R>>() {
    override val name = if (name.isNotBlank()) name else "${left.name} | ${right.name}"
    override val consumptionType = ConsumptionType.Multiple

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Either<L, R>> {
        val leftResult = left.convert(arg, args, event)
        val rightResult = right.convert(arg, args, event)

        return when {
            leftResult is ArgumentResult.Success -> ArgumentResult.Success(Either.Left(leftResult.result), leftResult.consumed)
            rightResult is ArgumentResult.Success -> ArgumentResult.Success(Either.Right(rightResult.result), rightResult.consumed)
            else -> ArgumentResult.Error("Could not match input with either expected argument.")
        }
    }

    override fun generateExamples(event: CommandEvent<*>): MutableList<String> {
        val leftExample = left.generateExamples(event).takeIf { it.isNotEmpty() }?.random() ?: "<Example>"
        val rightExample = right.generateExamples(event).takeIf { it.isNotEmpty() }?.random() ?: "<Example>"

        return mutableListOf("$leftExample | $rightExample")
    }
}

infix fun <L, R> ArgumentType<L>.or(right: ArgumentType<R>) = EitherArg(this, right)