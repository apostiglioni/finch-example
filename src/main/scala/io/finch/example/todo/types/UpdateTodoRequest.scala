package io.finch.example.todo.types

import java.util.UUID

final case class UpdateTodoRequest(uuid: UUID, changes: UpdateTodo)
