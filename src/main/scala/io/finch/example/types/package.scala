package io.finch.example

import cats.data.Coproduct
import cats.free.Free
import io.finch.example.log.dsl.grammar.{Computation => LComputation, Grammar => LGrammar}
import io.finch.example.todo.crud.dsl.grammar.{Computation => CComputation, Grammar => CGrammar}

package object types {
  type DSL[A] = Coproduct[CComputation, LComputation, A]

  type FreeDSL[O] = Free[DSL, O]

  type LogGrammar = LGrammar[DSL]
  type CrudGrammar = CGrammar[DSL]
}
