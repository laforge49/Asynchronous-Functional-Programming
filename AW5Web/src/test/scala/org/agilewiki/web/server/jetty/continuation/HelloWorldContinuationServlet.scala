package org.agilewiki
package web
package server
package jetty
package continuation

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.eclipse.jetty.continuation.ContinuationSupport
import util.actors._

class HelloWorldContinuationServlet extends AbstractContinuationServlet {
  override def createTarget(request: HttpServletRequest) = {
    new InternalAddressFuture(null)
  }

  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    super.doGet(request, response)
    val cont = ContinuationSupport.getContinuation(request)
    if (!cont.isResumed) {
      cont.getServletResponse.getOutputStream.print("Hello World")
      cont.complete
    }
  }
}
