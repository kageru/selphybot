package moe.kageru.kagebot.extensions

import arrow.Kind
import arrow.core.Either
import arrow.core.Tuple3
import arrow.core.getOrElse
import arrow.typeclasses.Functor

fun <L, R> Either<L, R>.on(op: (R) -> Unit): Either<L, R> {
    this.map { op(it) }
    return this
}

fun <T> Either<*, T>.unwrap(): T = getOrElse { error("Attempted to unwrap Either.left") }

fun <A, B, C, A2, F> Tuple3<A, B, C>.mapFirst(AP: Functor<F>, op: (A) -> Kind<F, A2>) = let { (a, b, c) ->
    AP.run { op(a).map { Tuple3(it, b, c) } }
}

fun <A, B, C, B2, F> Tuple3<A, B, C>.mapSecond(AP: Functor<F>, op: (B) -> Kind<F, B2>) = let { (a, b, c) ->
    AP.run { op(b).map { Tuple3(a, it, c) } }
}
