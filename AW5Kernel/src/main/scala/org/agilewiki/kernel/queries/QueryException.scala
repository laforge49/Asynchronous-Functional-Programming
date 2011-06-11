package org.agilewiki
package kernel
package queries

abstract case class QueryException(message: String, cause: Throwable) extends Exception(message, cause) {
  def this(message: String) = this (message, null)

  def this() = this (null)
}
