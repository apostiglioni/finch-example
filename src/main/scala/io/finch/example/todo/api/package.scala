package io.finch.example.todo

import java.util.UUID

import com.twitter.finagle.http.Status
import io.finch.example.todo.crud.dsl.Grammar
import io.finch.example.todo.types._
import io.finch.{Created, Ok, Output}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

package object api {

  private def asTodoResource = todoResourceFactory("http://localhost:8081/resource")

  def getMany(criteria: TodoQueryFilters)(implicit C: Grammar[TodoDSL]): FreeDSL[Future[Output[Seq[TodoResource]]]] = {
    for {
      eventually <- C.findAll(criteria)
      resources   = for { todos <- eventually } yield todos map asTodoResource
    } yield resources map Ok
  }

  def put(id: UUID, changes: UpdateTodo)(implicit C: Grammar[TodoDSL]): FreeDSL[Future[Output[TodoResource]]] = {
    for {
      maybeUpdated <- C.update(id, changes)
    } yield maybeUpdated map {
      case Some(todo) => Ok(asTodoResource(todo))
      case None       => Output empty Status.NotFound
    }
  }

  def getOne(id: UUID)(implicit C: Grammar[TodoDSL]): FreeDSL[Future[Output[TodoResource]]] = {
    for {
      maybeTodo <- C.findById(id)
    } yield maybeTodo map {
      case Some(todo) => Ok(asTodoResource(todo))
      case None       => Output empty Status.NotFound
    }
  }

  def post(update: UpdateTodo)(implicit C: Grammar[TodoDSL]): FreeDSL[Future[Output[TodoResource]]] = {
    for {
      saved <- (update andThen C.add)(builders.emptyTodo)
    } yield saved map (asTodoResource andThen Created)
  }

  def delete(id: UUID)(implicit C: Grammar[TodoDSL]): FreeDSL[Future[Output[Unit]]] = {
    for {
      deleted <- C.delete(id)
    } yield deleted map {
      case Left(_)  => Output empty Status.NotFound
      case Right(_) => Output empty Status.Ok
    }
  }
}
