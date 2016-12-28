package io.finch.example.log.dsl.interpreters

import cats.~>
import com.twitter.util.Future
import io.finch.example.log.dsl.grammar.{Computation, Debug}

object PrintInterpreter extends (Computation ~> Future) {

  override def apply[A](fa: Computation[A]): Future[A] = fa match {
    case Debug(msg, Some(exception)) => Future.value(debugException(msg, exception))
    case Debug(msg, None)            => Future.value(debugMessage(msg))
  }

  private def debugMessage(msg: String) = println(msg)
  private def debugException(msg: String, exception: Exception) = println(s"$msg\n$exception")
}
