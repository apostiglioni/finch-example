package io.finch.example

import cats.~>
import com.twitter.finagle.Http
import com.twitter.util.{Await, Future}
import io.circe.generic.auto._
import io.finch.Application
import io.finch.circe._
import io.finch.example.log.dsl.interpreters.PrintInterpreter
import io.finch.example.todo.crud.interpreters.H2CrudInterpreter
import io.finch.example.todo.endpoints
import io.finch.example.types.DSL

object Server extends App {

  val interpreter: DSL ~> Future = new H2CrudInterpreter("h2mem") or PrintInterpreter
  val services = endpoints.all("http://localhost:8081")(interpreter).toServiceAs[Application.Json]

  Await.ready(Http.server.serve(":8081", services))
}
