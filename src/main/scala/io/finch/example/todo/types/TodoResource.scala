package io.finch.example.todo.types

import io.finch.example.rest.types.LinkResource

final case class TodoResource
                 ( title: String
                 , body: Option[String]
                 , _links: List[LinkResource])
