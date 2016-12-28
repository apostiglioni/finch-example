package io.finch.example.todo

package object types {
  type UpdateTodo = Todo => Todo
  type TodoResourceFactory = (Todo) => TodoResource

}
