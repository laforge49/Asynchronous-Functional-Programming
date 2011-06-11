package org.agilewiki
package kernel

import java.util.Iterator

class NoRemoveStringIterator(iterator: Iterator[String]) extends Iterator[String] {
  protected val it: Iterator[String] = iterator

  override def hasNext = {it.hasNext()}

  override def next = {it.next()}

  override def remove {throw new UnsupportedOperationException()}

}
