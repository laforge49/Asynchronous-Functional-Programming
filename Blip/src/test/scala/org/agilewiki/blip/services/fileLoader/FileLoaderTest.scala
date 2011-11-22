package org.agilewiki.blip
package services
package fileLoader

import org.specs.SpecificationWithJUnit
import java.io.{BufferedReader, InputStreamReader, ByteArrayInputStream, File}
import bind._

case class DoIt()

class ShortFileLoader extends AsyncActor {
  bind(classOf[DoIt], doit)

  def doit(msg: AnyRef, rf: Any => Unit) {
    val cwd = new File(".")
    println("cwd = " + cwd.getCanonicalPath)
    val file = new File("aShortTestFile.txt")
    println("test file exists = " + file.exists)
    systemServices(LoadFile(file))(rf)
  }
}

class FileLoaderTest extends SpecificationWithJUnit {
  "FileLoaderTest" should {
    "load" in {
      val systemServices = SystemServices(new FileLoaderComponentFactory)
      try {
        val shortFileLoader = new ShortFileLoader
        shortFileLoader.setSystemServices(systemServices)
        val bytes = Future(shortFileLoader, DoIt()).asInstanceOf[Array[Byte]]
        val bais = new ByteArrayInputStream(bytes)
        val isr = new InputStreamReader(bais)
        val br = new BufferedReader(isr)
        val line = br.readLine
        println(line)
      } finally {
        systemServices.close
      }
    }
  }
}
