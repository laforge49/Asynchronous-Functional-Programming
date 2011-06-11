import org.agilewiki.actors.application.notification.Notification
import org.agilewiki.actors.application.notification.ChangeNotification

Notification notification
ChangeNotification change = notification.change()

def synchKeywords = {
  println(">>>>>>>>>>>> changes: " + change.changes())
}

synchKeywords()