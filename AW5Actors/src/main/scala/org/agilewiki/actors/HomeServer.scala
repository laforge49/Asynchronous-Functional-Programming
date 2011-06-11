package org.agilewiki.actors

import org.agilewiki.util.{SystemComposite, RolonName}
import org.agilewiki.kernel.Kernel

object HomeServer{
  def apply(roleName: String, systemContext: SystemComposite): String = {
    Kernel(systemContext).arkManager(roleName)
  }

  def apply(rolonName: RolonName, systemContext: SystemComposite): String = {
    if (rolonName.uuidExtension != null) apply(rolonName.uuidExtension, systemContext)
    else {
      val uuid = rolonName.rolonUuid
      val i = uuid.indexOf("_")
      val roleName = uuid.substring(i+1)
      apply(roleName, systemContext)
    }
  }
}