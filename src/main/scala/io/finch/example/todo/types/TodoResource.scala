package io.finch.example.todo.types

final case class TodoResource
                 ( title: String
                 , body: Option[String]
                 , _links: List[LinkResource])
