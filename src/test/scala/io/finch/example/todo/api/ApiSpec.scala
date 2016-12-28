package io.finch.example.todo.api

import cats.{Applicative, Id, ~>}
import io.finch.example.log.dsl.grammar.{Computation => LogComputation, _}
import io.finch.example.todo.crud.dsl.grammar.{FindAll, Computation => CrudComputation, _}
import io.finch.example.todo.types.{Todo, TodoQueryFilters}
import io.finch.example.types.{DSL, FreeDSL}
import org.scalatest._

import scala.collection.mutable.ListBuffer

class ApiSpec extends WordSpec with MustMatchers {

  trait EffectsCapturingInterpreter[T[A]] extends (T ~> Id) {
    val effects = new ListBuffer[T[_]]

    override def apply[A](fa: T[A]): Id[A] = {
      effects append fa
      Applicative[Id].pure (eval(fa))
    }

    def eval[A](fa: T[A]): A
  }

  def scenario(dsl: => FreeDSL[Seq[Todo]])(expectedCrudEffects: Any*)(expectedLogEffects: Any*): Assertion = {
    object CrudEffectsCapturingInterpreter extends EffectsCapturingInterpreter[CrudComputation] {
      override def eval[A](fa: CrudComputation[A]): A = fa match {
        // Dumb implementation with hardcoded data
        case Add(todo)           => todo                                            .asInstanceOf[A]
        case FindById(id)        => Todo(id, "Title", Some("body"))                 .asInstanceOf[A]
        case FindAll(_)          => Seq[Todo]()                                     .asInstanceOf[A]
        case Update(id, changes) => (changes apply Todo(id, "Title", Some("body"))) .asInstanceOf[A]
        case Delete(id)          => Right(Unit)                                     .asInstanceOf[A]
      }
    }

    object LogEffectsCapturingInterpreter extends EffectsCapturingInterpreter[LogComputation] {
      // Dumb implementation with hardcoded data
      override def eval[A](fa: LogComputation[A]): A = ().asInstanceOf[A]
    }

    val interpreter: DSL ~> Id = CrudEffectsCapturingInterpreter or LogEffectsCapturingInterpreter

    dsl foldMap interpreter

    CrudEffectsCapturingInterpreter.effects must contain theSameElementsInOrderAs expectedCrudEffects
    LogEffectsCapturingInterpreter.effects  must contain theSameElementsInOrderAs expectedLogEffects
  }

  "Get should trigger effects" in {
    val filters = TodoQueryFilters(Some("title"), Some("body"))
    (scenario
      (for { many <- getMany(filters) } yield many)
      (FindAll(filters))
      (Debug("GET for todos", None)))
  }
}
