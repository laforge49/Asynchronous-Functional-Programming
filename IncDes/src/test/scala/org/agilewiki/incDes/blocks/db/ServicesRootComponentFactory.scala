package org.agilewiki
package incDes
package blocks
package db

import blip._
import services._

class ServicesRootComponentFactory
  extends ComponentFactory {
  addDependency(classOf[FactoryRegistryComponentFactory])
  addDependency(classOf[ActorRegistryComponentFactory])
}
