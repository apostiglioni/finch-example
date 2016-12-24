package io.finch.example.todo

package object types {
  type UpdateTodo = Todo => Todo

  def toResource[S <: Identifiable, D](factory: (S, LinkResource) => D)(baseLocation: String)(source: S): D =
    factory(source, selfLink(s"$baseLocation/${source.id}"))

  def selfLink(location: String) = LinkResource("self", location)

  def todoResourceFactory: (String) => (Todo) => TodoResource =
    toResource((todo: Todo, self: LinkResource) => TodoResource(todo.title, todo.body, List(self)))
}
