package io.finch.example.log.dsl

import cats.free.Free.inject
import cats.free.{Free, Inject}

package object grammar {
  sealed trait Computation[T]
  final case class Debug(msg: String, exception: Option[Exception]) extends Computation[Unit]

  class Grammar[F[_]](implicit I: Inject[Computation, F]) {
    def debug(msg: String)                      : Free[F, Unit] = inject(Debug(msg, None))
    def debug(msg: String, exception: Exception): Free[F, Unit] = inject(Debug(msg, Some(exception)))
  }

  object Grammar {
    implicit def grammar[F[_]](implicit I: Inject[Computation, F]): Grammar[F] = new Grammar[F]
  }
}
