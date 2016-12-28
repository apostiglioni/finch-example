package io.finch.example.todo.types

import java.util.UUID

import io.finch.example.rest.types._
import io.finch.example.rest.types.builders._

package object builders {
  def todo(title: String, body: Option[String]) = Todo(UUID.randomUUID(), title, body)

  def emptyTodo = Todo(UUID.randomUUID(), "", None)

  def todoResourceAbstractFactory: (String) => TodoResourceFactory =
    toResource((todo: Todo, self: LinkResource) => TodoResource(todo.title, todo.body, List(self)))
}
