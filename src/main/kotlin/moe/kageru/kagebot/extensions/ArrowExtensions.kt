package moe.kageru.kagebot.extensions

import arrow.core.Either

fun <L, R> Either<L, R>.on(op: (R) -> Unit): Either<L, R> {
    this.map { op(it) }
    return this
}
