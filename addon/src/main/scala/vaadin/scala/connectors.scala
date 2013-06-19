package vaadin.scala

import vaadin.scala.mixins._
import vaadin.scala.event.ConnectorEvent
import vaadin.scala.internal.ListenersTrait
import vaadin.scala.internal.DetachListener
import com.vaadin.server.AbstractClientConnector
import vaadin.scala.event.DetachEvent

package mixins {

  trait ConnectorMixin extends ScaladinMixin { self: com.vaadin.shared.Connector =>
  }

  trait ClientConnectorMixin extends ConnectorMixin { self: com.vaadin.server.ClientConnector =>
    override def wrapper = super.wrapper.asInstanceOf[ClientConnector]

  }
}

trait Connector extends Wrapper {
  def p: com.vaadin.shared.Connector with ConnectorMixin

  def connectorId: String = p.getConnectorId()
  def parent: Option[Connector] = wrapperFor(p.getParent())
}

trait ClientConnector extends Connector {
  def p: com.vaadin.server.ClientConnector with ClientConnectorMixin

  /* Overriding attach or detach will not affect anything.
   * Use listeners instead. */
  def attach(): Unit = p.attach()
  def detach(): Unit = p.detach()

  def parent_=(parentConnector: ClientConnector): Unit = if (parentConnector != null) p.setParent(parentConnector.p) else p.setParent(null)
  def parent_=(parentConnector: Option[ClientConnector]): Unit = p.setParent(parentConnector map ((c: ClientConnector) => c.p) orNull)

  /* p.asInstanceOf[AbstractClientConnector]
   * Bit of an assumption, it means we only support implementations of ClientConnector that are actually AbstractClientConnectors. 
   * This is needed if we want to handle listeners here, since getListeners is defined in the abstract class. */

  lazy val detachListeners = new ListenersTrait[DetachEvent, DetachListener] {

    override def listeners = p.asInstanceOf[AbstractClientConnector].getListeners(classOf[com.vaadin.server.ClientConnector.DetachEvent])
    override def addListener(elem: DetachEvent => Unit) = p.addDetachListener(new DetachListener(elem))
    override def removeListener(elem: DetachListener) = p.removeDetachListener(elem)
  }
}