package org.agilewiki
package util

import org.slf4j.{Logger => SLF4JLogger, LoggerFactory}

object Logger {
  import org.apache.log4j.LogManager
  import org.apache.log4j.xml.DOMConfigurator
  private var inited = false

  private def init {
    if(!inited){
      inited = true
      val url = ClassLoader.getSystemClassLoader.getResource("log4j.xml")
      println("Loading: " + url.getFile)
      new DOMConfigurator().doConfigure(url, LogManager.getLoggerRepository)
    }
  }

  def loggerNameOf(cls:Class[_])={
    init
    var name = cls.getName
    while(name endsWith  "$") name = name.substring(0,name.length - 1)
    name
  }
}

private trait CustomLogger {
  protected lazy val logger:SLF4JLogger = _logger
  private def _logger = LoggerFactory.getLogger(Logger loggerNameOf this.getClass)

  def trace(msg: => AnyRef) = if(_logger.isTraceEnabled) _logger.trace(String.valueOf(msg))

  def debug(msg: => AnyRef) = if(_logger.isDebugEnabled) _logger.debug(String.valueOf(msg))

  def debug(msg: => AnyRef, t: Throwable) = if(_logger.isDebugEnabled) _logger.debug(String.valueOf(msg),t)

  def info(msg: => AnyRef) = if(_logger.isInfoEnabled) _logger.info(String.valueOf(msg))

  def info(msg: => AnyRef, t: Throwable) = if(_logger.isInfoEnabled)   _logger.info(String.valueOf(msg),t)

  def warn(msg: => AnyRef) = if(_logger.isWarnEnabled) _logger.warn(String.valueOf(msg))

  def warn(msg: => AnyRef, t: Throwable) = if(_logger.isWarnEnabled) _logger.warn(String.valueOf(msg),t)

  def error(msg: => AnyRef) = if(_logger.isErrorEnabled) _logger.error(String.valueOf(msg))

  def error(msg: => AnyRef, t: Throwable) = if(_logger.isErrorEnabled) _logger.error(String.valueOf(msg),t)

}

trait Logger {

  def trace(msg: => AnyRef) = {}

  def debug(msg: => AnyRef) = {}

  def debug(msg: => AnyRef, t: Throwable) = {}

  def info(msg: => AnyRef) = {}

  def info(msg: => AnyRef, t: Throwable) = {}

  def warn(msg: => AnyRef) = {}

  def warn(msg: => AnyRef, t: Throwable) = {}

  def error(msg: => AnyRef) = {System.err.println(String.valueOf(msg))}

  def error(msg: => AnyRef, t: Throwable) = {System.err.println(String.valueOf(msg)); t.printStackTrace(System.err)}

}

trait Loggable {
  val logger = new Object with Logger
}