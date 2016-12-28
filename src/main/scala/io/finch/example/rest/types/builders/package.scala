package io.finch.example.rest.types

import io.finch.example.types.Identifiable

package object builders {

  def toResource[S <: Identifiable, D](factory: (S, LinkResource) => D)(baseLocation: String)(source: S): D =
    factory(source, selfLink(s"$baseLocation/${source.id}"))

  def selfLink(location: String) = LinkResource("self", location)

}
