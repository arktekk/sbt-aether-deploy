package aether

import org.apache.maven.wagon.Wagon
import org.eclipse.aether.transport.wagon.{WagonConfigurator, WagonProvider}

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
case class WagonWrapper(scheme: String, wagon: Class[Wagon]) {
  def newWagon: Wagon = wagon.newInstance()
}

object WagonWrapper {
  def apply(scheme: String, clazz: String): WagonWrapper = {
    WagonWrapper(scheme, Class.forName(clazz).asInstanceOf[Class[Wagon]])
  }
}

class ExtraWagonProvider(wagons: Seq[WagonWrapper]) extends WagonProvider {
  private val map = wagons.map(w => w.scheme -> w.wagon).toMap

  def lookup(roleHint: String): Wagon = {
    map.get(roleHint).map(_.newInstance()).getOrElse(throw new IllegalArgumentException("Unknown wagon type"))
  }

  def release(wagon: Wagon) {
    try {
      if (wagon != null) wagon.disconnect()
    }
    catch {
      case e: Exception => e.printStackTrace()
    }
  }
}

object NoOpWagonConfigurator extends WagonConfigurator {
  def configure(wagon: Wagon, configuration: Any) {
  }
}