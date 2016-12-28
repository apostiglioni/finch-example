package io.finch.example.todo.endpoints

import java.util.UUID

import cats.~>
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import io.catbird.util._
import io.circe.generic.auto._
import io.finch.circe._
import io.finch.example.todo.types._
import io.finch.example.todo.types.builders._
import io.finch.example.todo.{api, endpoints}
import io.finch.example.types.{FreeDSL, DSL}
import io.finch.{Created, Endpoint, Ok, Output, jsonBody, paramOption, uuid, delete => DELETE, get => GET, post => POST, put => PUT}

trait Endpoints {
  type FreeEndpoint[T] = Endpoint[FreeDSL[Output[T]]]

  private val filters = (paramOption("title") :: paramOption("body")).as[TodoQueryFilters]
  def filter(asTodoResource: TodoResourceFactory): FreeEndpoint[Seq[TodoResource]] = {
    GET("todos" :: filters).map { criteria: TodoQueryFilters =>
      for {
        todos <- api.getMany(criteria)
        resources = todos map asTodoResource
      } yield Ok(resources)
    }
  }

    def get(asTodoResource: TodoResourceFactory): FreeEndpoint[TodoResource] = {
      GET("todos" :: uuid).map { id: UUID =>
        for {
          maybeTodo <- api.getOne(id)
        } yield maybeTodo match {
          case Some(todo) => Ok(asTodoResource(todo))
          case None       => Output empty Status.NotFound
        }
      }
    }

    private val updateTodoRequest = (uuid :: jsonBody[UpdateTodo]).as[UpdateTodoRequest]
    def put(asTodoResource: TodoResourceFactory): FreeEndpoint[TodoResource] = {
      PUT("todos" :: updateTodoRequest).map { request: UpdateTodoRequest =>
        for {
          maybeUpdated <- api.put(request.uuid, request.changes)
        } yield maybeUpdated match {
          case Some(todo) => Ok(asTodoResource(todo))
          case None       => Output empty Status.NotFound
        }
      }
    }

    def post(asTodoResource: TodoResourceFactory): FreeEndpoint[TodoResource] = {
      POST("todos" :: jsonBody[UpdateTodo]).map { changes: UpdateTodo =>
        for {
          saved <- api.post(changes)
        } yield (asTodoResource andThen Created)(saved)
      }
    }

    def delete: FreeEndpoint[Unit] = {
      DELETE("todos" :: uuid).map { id: UUID =>
        for {
          deleted <- api.delete(id)
        } yield deleted match {
          case Left(_)  => Output empty Status.NotFound
          case Right(_) => Output empty Status.Ok
        }
      }
    }


  def all(location: String)(interpreter: DSL ~> Future) = {
    val asTodoResource = todoResourceAbstractFactory(location)

      (   (endpoints.filter (asTodoResource) mapOutputAsync { f => f foldMap interpreter })
      :+: (endpoints.get    (asTodoResource) mapOutputAsync { f => f foldMap interpreter })
      :+: (endpoints.post   (asTodoResource) mapOutputAsync { f => f foldMap interpreter })
      :+: (endpoints.put    (asTodoResource) mapOutputAsync { f => f foldMap interpreter })
      :+: (endpoints.delete                  mapOutputAsync { f => f foldMap interpreter })
      )
  }
}

object Endpoints extends Endpoints
