package io.finch.example.todo.crud.interpreters

import java.util.UUID

import cats.~>
import com.twitter.util.{Future, Promise}
import io.finch.example.todo.crud.dsl.grammar._
import io.finch.example.todo.db.schema.Todos
import io.finch.example.todo.types.{Todo, TodoQueryFilters, UpdateTodo}
import slick.driver.H2Driver.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future => SFuture}
import scala.util.{Failure, Success}

class H2CrudInterpreter(path: String) extends (Computation ~> Future) {
  val db = Database.forConfig(path)

  db run DBIO.seq(
    // Create the tables, including primary and foreign keys
    TableQuery[Todos].schema.create,

    // Insert some todos
    TableQuery[Todos] += Todo(UUID.randomUUID(), "First todo", Some("This is my first todo"))
  )

  override def apply[A](fa: Computation[A]): Future[A] = fa match {
    case Add(todo)           => add(todo)           .asInstanceOf[Future[A]]
    case FindById(id)        => findById(id)        .asInstanceOf[Future[A]]
    case FindAll(criteria)   => findAll(criteria)   .asInstanceOf[Future[A]]
    case Update(id, changes) => update(id, changes) .asInstanceOf[Future[A]]
    case Delete(id)          => delete(id)          .asInstanceOf[Future[A]]
  }

  private def update(id: UUID, changes: UpdateTodo): Future[Option[Todo]] = {
    findById(id) map (maybeFound => maybeFound map changes.apply) flatMap {
      case None          => Future.value(None)
      case Some(updated) => insertOrUpdate(updated)
    }
  }

  private def delete(id: UUID): Future[Either[String, Unit]] = {
    val q = TableQuery[Todos] filter(_.id === id)
    asTwitter(db run q.delete map {
      case 0 => Left("Not found")
      case 1 => Right(Unit)
    })
  }

  private def findAll(criteria: TodoQueryFilters) = {
    val q = TableQuery[Todos] filter { todo =>

      ((List(
          criteria.title map (todo.title === _)
        , criteria.body  map (body => todo.body map (_ === body) getOrElse (true: Rep[Boolean]))
      ) collect { case Some(filter) => filter }
      ) reduceLeftOption (_ && _)
      ) getOrElse (true: Rep[Boolean])
    }

    asTwitter(db run q.result)
  }

  private def insertOrUpdate(todo: Todo) = {
    val q = for {
      result <- TableQuery[Todos] insertOrUpdate todo
    } yield result

    asTwitter(db run q map {
      case 1 => Some(todo)
      case _ => None
    })
  }

  private def findById(id: UUID): Future[Option[Todo]] =  {
    val q = TableQuery[Todos].filter(_.id === id)

    asTwitter(db run q.result map (_.headOption))
  }

  private def add(todo: Todo)  = {
    val action = TableQuery[Todos] += todo
    asTwitter(db run DBIO.seq(action) map (_ => todo))
  }

  def asTwitter[A](f: SFuture[A]): Future[A] = {
    val p: Promise[A] = new Promise[A]

    f onComplete {
      case Success(value) => p setValue value
      case Failure(exception) => p setException exception
    }

    p
  }
}

