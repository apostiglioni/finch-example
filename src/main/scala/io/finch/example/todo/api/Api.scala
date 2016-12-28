package io.finch.example.todo.api

import java.util.UUID

import io.finch.example.todo.types.{Todo, TodoQueryFilters, UpdateTodo, builders}
import io.finch.example.types.{CrudGrammar, FreeDSL, LogGrammar}

trait Api {
  def getMany(criteria: TodoQueryFilters)(implicit C: CrudGrammar, L: LogGrammar): FreeDSL[Seq[Todo]] = {
    for {
      _     <- L.debug("GET for todos")
      todos <- C.findAll(criteria)
    } yield todos
  }

  def put(id: UUID, changes: UpdateTodo)(implicit C: CrudGrammar, L: LogGrammar): FreeDSL[Option[Todo]] = {
    for {
      maybeUpdated <- C.update(id, changes)
    } yield maybeUpdated
  }

  def getOne(id: UUID)(implicit C: CrudGrammar, L: LogGrammar): FreeDSL[Option[Todo]] = {
    for {
      maybeTodo <- C.findById(id)
    } yield maybeTodo
  }

  def post(update: UpdateTodo)(implicit C: CrudGrammar, L: LogGrammar): FreeDSL[Todo] = {
    for {
      saved <- (update andThen C.add)(builders.emptyTodo)
    } yield saved
  }

  def delete(id: UUID)(implicit C: CrudGrammar): FreeDSL[Either[String, Unit]] = {
    for {
      deleted <- C.delete(id)
    } yield deleted
  }
}
