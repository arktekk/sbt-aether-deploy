package aether

import org.eclipse.aether.util.repository.DefaultProxySelector
import util.Properties
import org.eclipse.aether.repository.{Authentication, Proxy => AProxy}
import java.net.URI

/*
 *  http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html
 */
private[aether] object SystemPropertyProxySelector {
  /*loadProxies().foreach( p => {
    add(p, Properties.envOrNone("http.nonProxyHosts").orNull)
  })

  /**
   * java -Dhttp.proxyHost=myproxy -Dhttp.proxyPort=8080 -Dhttp.proxyUser=username -Dhttp.proxyPassword=mypassword
   * @return
   */
  private def loadProxies(): Option[AProxy] = {
    val env = Properties.envOrNone("http_proxy").map(URI.create).map(uri => {
      val port = uri.getScheme -> uri.getPort match {
        case ("http", -1) => 80
        case ("https", -1) => 443
        case (_, p) => p
      }
      new AProxy(uri.getScheme, uri.getHost, port, null)
    })
    val http = Properties.propOrNone("http.proxyHost").map(host => new AProxy("http", host, Properties.propOrElse("http.proxyPort", "80").toInt, null))
    val https = Properties.propOrNone("https.proxyHost").map(host => new AProxy("https", host, Properties.propOrElse("https.proxyPort", "443").toInt, null))

    val auth = (Properties.propOrNone("http.proxyUser") -> Properties.propOrNone("http.proxyPassword")) match {
      case (Some(u), Some(p)) => new StringAuthentication(u, p)
      case _ => null
    }

    env.orElse(http).orElse(https).map(u => u.setAuthentication(auth))
  }
*/
}
