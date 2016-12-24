package io.finch.example.todo

import io.finch.example.todo.types._
import java.util.UUID

import io.circe.generic.auto._
import io.finch.circe._
import io.finch.{Endpoint, jsonBody, paramOption, uuid, delete => DELETE, get => GET, post => POST, put => PUT}

import scala.concurrent.ExecutionContext.Implicits.global
import io.finch.example.Runtime._

package object endpoints {

  val filters = (paramOption("title") :: paramOption("body")).as[TodoQueryFilters]
  def filter: Endpoint[Seq[TodoResource]] = {
    GET("todos" :: filters) {
      (criteria: TodoQueryFilters) => {
        api.getMany(criteria)
      }
    }
  }

  def get: Endpoint[TodoResource] = GET("todos" :: uuid) {
    (id: UUID) => {
      api.getOne(id)
    }
  }

  val updateTodoRequest: Endpoint[UpdateTodoRequest] = (uuid :: jsonBody[UpdateTodo]).as[UpdateTodoRequest]
  def put: Endpoint[TodoResource] = PUT("todos" :: updateTodoRequest) {
    (request: UpdateTodoRequest) => {
      api.put(request.uuid, request.changes)
    }
  }

  def post: Endpoint[TodoResource] = POST("todos" :: jsonBody[UpdateTodo]) {
    (changes: UpdateTodo) => {
      api.post(changes)
    }
  }

  def delete: Endpoint[Unit] = DELETE("todos" :: uuid) {
    (id: UUID) => {
      api.delete(id)
    }
  }
}
