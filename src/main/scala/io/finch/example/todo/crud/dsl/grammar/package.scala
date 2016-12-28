package io.finch.example.todo.crud.dsl

import java.util.UUID

import cats.free.Free.inject
import cats.free.{Free, Inject}
import io.finch.example.todo.types.{Todo, TodoQueryFilters, UpdateTodo}

package object grammar {
  sealed trait Computation[T]
  final case class FindById(id: UUID)                    extends Computation[Option[Todo]]
  final case class FindAll(criteria: TodoQueryFilters)   extends Computation[Seq[Todo]]
  final case class Add(data: Todo)                       extends Computation[Todo]
  final case class Update(id: UUID, changes: UpdateTodo) extends Computation[Option[Todo]]
  final case class Delete(id: UUID)                      extends Computation[Either[String, Unit]]


  class Grammar[F[_]](implicit I: Inject[Computation, F]) {
    def findById(id: UUID)                   : Free[F, Option[Todo]]         = inject(FindById(id))
    def findAll(criteria: TodoQueryFilters)  : Free[F, Seq[Todo]]            = inject(FindAll(criteria))
    def add(data: Todo)                      : Free[F, Todo]                 = inject(Add(data))
    def update(id: UUID, changes: UpdateTodo): Free[F, Option[Todo]]         = inject(Update(id, changes))
    def delete(id: UUID)                     : Free[F, Either[String, Unit]] = inject(Delete(id))
  }

  object Grammar {
    implicit def grammar[F[_]](implicit I: Inject[Computation, F]): Grammar[F] = new Grammar[F]
  }
}
