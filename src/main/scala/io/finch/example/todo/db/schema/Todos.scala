package io.finch.example.todo.db.schema

import java.util.UUID

import io.finch.example.todo.types.Todo
import slick.driver.H2Driver.api._

// Definition of the TODOS table
class Todos(tag: Tag) extends Table[Todo](tag, "TODOS") {
  def id    = column[UUID]            ("id", O.PrimaryKey)
  def title = column[String]          ("title")
  def body  = column[Option[String]]  ("body")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, title, body) <> (Todo.tupled, Todo.unapply)
}
