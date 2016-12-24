package io.finch.example.todo.crud.interpreters

import java.util.UUID

import cats.{Applicative, Id, ~>}
import io.finch.example.todo.crud.dsl._
import io.finch.example.todo.db.schema.Todos
import io.finch.example
import io.finch.example.todo.types.{Todo, TodoQueryFilters, UpdateTodo}
import slick.dbio.Effect.Write
import slick.driver.H2Driver.api._
import slick.lifted.TableQuery
import slick.profile.FixedSqlAction

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object InMemoryH2CrudInterpreter extends (Computation ~> Id) {
  private val db = Database.forConfig("h2mem")

  db run DBIO.seq(
    // Create the tables, including primary and foreign keys
    TableQuery[Todos].schema.create,

    // Insert some todos
    TableQuery[Todos] += Todo(UUID.randomUUID(), "First todo", Some("This is my first todo"))
  )

  private def update(id: UUID, changes: UpdateTodo)(implicit target: Applicative[Id]): Future[Option[Todo]] = {
    findById(id) map (maybeFound => maybeFound map changes.apply) flatMap {
      case None          => target pure Future.successful(None)
      case Some(updated) => insertOrUpdate(updated)
    }
  }

  private def delete(id: UUID)(implicit target: Applicative[Id]): Id[Future[Either[String, Unit]]] = {
    val q = TableQuery[Todos] filter(_.id === id)
    target pure (db run q.delete map {
      case 0 => Left("Not found")
      case 1 => Right(Unit)
    })
  }

  override def apply[A](fa: Computation[A]): Id[A] = fa match {
    case Add(todo)           => add(todo)
    case FindById(id)        => findById(id)
    case FindAll(criteria)   => findAll(criteria)
    case Update(id, changes) => update(id, changes)
    case Delete(id)          => delete(id)
  }

  private def findAll(criteria: TodoQueryFilters)(implicit target: Applicative[Id]) = {
    val q = TableQuery[Todos] filter { todo =>

      ((List(
          criteria.title map (todo.title === _)
        , criteria.body  map (body => todo.body map (_ === body) getOrElse (true: Rep[Boolean]))
      ) collect { case Some(filter) => filter }
      ) reduceLeftOption (_ && _)
      ) getOrElse (true: Rep[Boolean])
    }

    target pure (db run q.result)
  }

  private def insertOrUpdate(todo: Todo)(implicit target: Applicative[Id]) = {
    val q = for {
      result <- TableQuery[Todos] insertOrUpdate todo
    } yield result

    target pure (db run q map {
      case 1 => Some(todo)
      case _ => None
    })
  }

  private[this] def findById(id: UUID)(implicit target: Applicative[Id]): Id[Future[Option[Todo]]] =  {
    val q = TableQuery[Todos].filter(_.id === id)

    target pure (db run q.result map (_.headOption))
  }

  private[this] def add(todo: Todo)(implicit target: Applicative[Id])  = {
    val action = TableQuery[Todos] += todo
    target pure (db run DBIO.seq(action) map (_ => todo))
  }
}
