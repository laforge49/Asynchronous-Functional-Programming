package org.agilewiki.core.batch

object Data {
  private val map = new ThreadLocal[java.util.HashMap[String, String]]
  map.set(new java.util.HashMap[String, String])

  def get(name: String) = map.get.get(name)

  def put(name: String, value: String) { map.get.put(name, value)}

  def clear {map.get.clear}
}