package io.finch.example.todo.types

import java.util.UUID

package object builders {
  def todo(title: String, body: Option[String]) = Todo(UUID.randomUUID(), title, body)
  def emptyTodo = Todo(UUID.randomUUID(), "", None)
}
