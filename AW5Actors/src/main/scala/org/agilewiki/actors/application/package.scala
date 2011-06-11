package org.agilewiki.actors

import application.query.RolonQueryActor
package object application {

  val EMPTY_RESPONSE = "EmptyResponse"
  val ROLON_DOES_NOT_EXIST_REPONSE = "RolonDoesNotExist"

  val ROLON_REQUEST = "RolonRequest"
  val ROLON_RESPONSE = "RolonReponse"
  val ROLON_ALREADY_EXISTS = "RolonAlreadyExists"
  val ROLON_OUT_OF_SYNC = "RolonOutOfSync"
  val ROLON_QUERY_ACTOR = classOf[RolonQueryActor].getName

  val NOTIFICATION_APPLICATION_DATA = "NotificationApplicationData"
  
  val SystemAttributes = Set("btreeSize","elementType","name","uuid") 
}