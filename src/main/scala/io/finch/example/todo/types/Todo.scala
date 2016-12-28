package io.finch.example.todo.types

import java.util.UUID

import io.finch.example.types.Identifiable

final case class Todo(id: UUID, title: String, body: Option[String]) extends Identifiable
