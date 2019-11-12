package moe.kageru.kagebot.extensions

import arrow.core.Either
import arrow.core.getOrElse

fun <L, R> Either<L, R>.on(op: (R) -> Unit): Either<L, R> {
    this.map { op(it) }
    return this
}

fun <T> Either<*, T>.unwrap(): T = getOrElse { error("Attempted to unwrap Either.left") }
