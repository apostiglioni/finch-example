package io.finch.example.todo.crud

import java.util.UUID

import cats.free.{Free, Inject}
import io.finch.example.todo.types.{Todo, TodoQueryFilters, UpdateTodo}

import scala.concurrent.Future

package object dsl {
  sealed trait Computation[T]
  final case class FindById(id: UUID)                    extends Computation[Future[Option[Todo]]]
  final case class FindAll(criteria: TodoQueryFilters)   extends Computation[Future[Seq[Todo]]]
  final case class Add(data: Todo)                       extends Computation[Future[Todo]]
  final case class Update(id: UUID, changes: UpdateTodo) extends Computation[Future[Option[Todo]]]
  final case class Delete(id: UUID)                      extends Computation[Future[Either[String, Unit]]]


  class Grammar[F[_]](implicit I: Inject[Computation, F]) {
    def findById(id: UUID): Free[F, Future[Option[Todo]]]                    = inject(FindById(id))
    def findAll(criteria: TodoQueryFilters): Free[F, Future[Seq[Todo]]]      = inject(FindAll(criteria))
    def add(data: Todo): Free[F, Future[Todo]]                               = inject(Add(data))
    def update(id: UUID, changes: UpdateTodo): Free[F, Future[Option[Todo]]] = inject(Update(id, changes))
    def delete(id: UUID): Free[F, Future[Either[String, Unit]]]              = inject(Delete(id))

    private def inject[T](action: Computation[T]) = Free.inject[Computation, F](action)
  }

  object Grammar {
    implicit def grammar[F[_]](implicit I: Inject[Computation, F]): Grammar[F] = new Grammar[F]
  }
}
