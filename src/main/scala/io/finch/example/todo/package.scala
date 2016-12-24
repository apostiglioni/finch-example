package io.finch.example

import cats.free.Free
import io.finch.example.todo.crud.dsl.Computation

package object todo {
  type TodoDSL[A] = Computation[A]
  type FreeDSL[O] = Free[TodoDSL, O]
}
