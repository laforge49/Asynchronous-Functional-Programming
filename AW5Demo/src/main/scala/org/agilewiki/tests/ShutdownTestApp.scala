package org.agilewiki
package tests

import java.net.{DatagramPacket, DatagramSocket, InetAddress}
import util.actors.res._
import util.com.DataOutputStack
import util.com.shrt.ShortProtocol

object ShutdownTestApp {
  def main(args: Array[String]): Unit = {
    println("Destination ark name: "+args(0))
    println("Destination host name: "+args(1))
    println("Destination port number: "+args(2))
    print("Sending shutdown command to test application...")
    val data = DataOutputStack()
    data writeUTF "shutdown"                    //Command name
    data writeUTF Uuid("ARK").toString              //Destination Actor Resource name
    data writeUTF "Manual Shutdown Message"     //Message UUID
    data writeUTF Uuid(ShortProtocol.SHORT_ACTOR).toString     //Reply Actor UUID
    data writeUTF "shutdown"                    //Request UUID
    data writeUTF "0"                           //Message type (request)
    data writeUTF args(0)                      //Destination Ark Name
    data writeUTF "Shutdowner"                  //Source Ark Name
    val buffer = data.getBytes
    val packet = new DatagramPacket(
      buffer,
      buffer.length,
      InetAddress.getByName(args(1)),
      args(2).toInt)
    val socket = new DatagramSocket
    socket send packet
    Thread.sleep(1000)
    println("\t\t[SENT]")
  }
}