package org.agilewiki
package web
package server

import util.SystemComposite

trait WebServer {
  def isWebServerStarted: Boolean

  def isWebServerStopped: Boolean

  def startWebServer(systemContext: SystemComposite): Unit

  def stopWebServer: Unit
}
