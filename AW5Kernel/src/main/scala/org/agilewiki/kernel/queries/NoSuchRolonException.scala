package org.agilewiki
package kernel
package queries

class NoSuchRolonException(message: String, cause: Throwable) extends QueryException(message, cause) {
  def this(message: String) = this (message, null)

  def this() = this (null)
}
