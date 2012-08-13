package aether

import org.apache.maven.wagon.Wagon

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
case class WagonWrapper(scheme: String, wagon: Wagon)
