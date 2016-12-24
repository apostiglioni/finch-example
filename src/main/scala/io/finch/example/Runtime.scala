package io.finch.example

import cats.free.Free
import cats.{Id, ~>}
import com.twitter.util.Promise
import io.finch.example.todo.crud.interpreters.InMemoryH2CrudInterpreter
import io.finch.example.todo.{FreeDSL, TodoDSL}
import io.finch.internal.Mapper
import io.finch.{Endpoint, Output}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Runtime {

  private val interpreter: TodoDSL ~> Id = InMemoryH2CrudInterpreter

  implicit def fromFreeMonadOutput[A, B](f: A => Free[TodoDSL, Output[B]]): Mapper.Aux[A, B] = new Mapper[A] {
    type Out = B

    def apply(e: Endpoint[A]): Endpoint[B] = { e mapOutput (f(_) foldMap interpreter) }
  }

  implicit def fromFreeMonadFutureOutput[A, B](f: A => FreeDSL[Future[Output[B]]])(implicit e: ExecutionContext): Mapper.Aux[A, B] = new Mapper[A] {
    type Out = B

    def apply(e: Endpoint[A]): Endpoint[B] = {
      e mapOutputAsync (input => {
        val p: Promise[Output[B]] = new Promise[Output[B]]

        f(input) foldMap interpreter onComplete {
          case Success(value) => p setValue value
          case Failure(exception) => p setException exception
        }

        p
      })
    }
  }
}
