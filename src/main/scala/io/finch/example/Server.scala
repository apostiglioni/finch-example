package io.finch.example

import com.twitter.finagle.Http
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch.Application
import io.finch.circe._
import io.finch.example.todo.endpoints

object Server extends App {
  val services = ( endpoints.filter
               :+: endpoints.get
               :+: endpoints.put
               :+: endpoints.post
               :+: endpoints.delete
               ).toServiceAs[Application.Json]

  Await.ready(Http.server.serve(":8081", services))
}
