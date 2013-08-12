package aether

import org.eclipse.aether.util.repository.{AuthenticationBuilder, DefaultProxySelector}
import util.Properties
import org.eclipse.aether.repository.{Proxy => AProxy, ProxySelector, Authentication}
import java.net.URI

/*
 *  http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html
 */
private[aether] object SystemPropertyProxySelector {
  lazy val selector = {
    val selector = new DefaultProxySelector
    loadProxies().foreach( p => {
      selector.add(p, envOrProp("http.nonProxyHosts").orNull)
    })
    selector
  }

  def apply(): ProxySelector = selector


  /**
   * java -Dhttp.proxyHost=myproxy -Dhttp.proxyPort=8080 -Dhttp.proxyUser=username -Dhttp.proxyPassword=mypassword
   * @return
   */
  private def loadProxies(): Option[AProxy] = {
    val auth = envOrProp("http.proxyUser") -> envOrProp("http.proxyPassword") match {
      case (Some(u), Some(p)) => new AuthenticationBuilder().addUsername(u).addPassword(p).build()
      case _ => null
    }

    val env = envOrProp("http_proxy").map(URI.create).map(uri => {
      val port = uri.getScheme -> uri.getPort match {
        case ("http", -1) => 80
        case ("https", -1) => 443
        case (_, p) => p
      }
      new AProxy(uri.getScheme, uri.getHost, port, auth)
    })

    val http = envOrProp("http.proxyHost").map(host => new AProxy("http", host, envOrProp("http.proxyPort").getOrElse("80").toInt, auth))
    val https = envOrProp("https.proxyHost").map(host => new AProxy("https", host, envOrProp("https.proxyPort").getOrElse("443").toInt, auth))

    env.orElse(http).orElse(https)
  }

  private def envOrProp(name: String): Option[String] = Properties.envOrNone(name).orElse(Properties.propOrNone(name))

}
