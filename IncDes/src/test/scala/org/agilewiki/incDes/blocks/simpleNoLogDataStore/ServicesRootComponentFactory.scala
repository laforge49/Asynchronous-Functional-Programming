package org.agilewiki
package incDes
package blocks
package simpleNoLogDataStore

import blip._
import services._

class ServicesRootComponentFactory
  extends ComponentFactory {
  addDependency(classOf[FactoryRegistryComponentFactory])
  addDependency(classOf[ActorRegistryComponentFactory])
}
