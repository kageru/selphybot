package moe.kageru.kagebot.extensions

import arrow.core.Either
import arrow.core.Option
import arrow.core.Tuple3
import arrow.core.getOrElse
import arrow.optics.pFirst
import arrow.optics.pSecond

fun <L, R> Either<L, R>.on(op: (R) -> Unit): Either<L, R> {
    this.map { op(it) }
    return this
}

fun <T> Either<*, T>.unwrap(): T = getOrElse { error("Attempted to unwrap Either.left") }

fun <A, B, C, R> Tuple3<A, B, C>.mapSecond(op: (B) -> Option<R>): Option<Tuple3<A, R, C>> {
    return op(this.b).map { Tuple3.pSecond<A, B, C, R>().set(this, it) }
}

fun <A, B, C, R> Tuple3<A, B, C>.mapFirst(op: (A) -> Option<R>): Option<Tuple3<R, B, C>> {
    return op(this.a).map { Tuple3.pFirst<A, B, C, R>().set(this, it) }
}
