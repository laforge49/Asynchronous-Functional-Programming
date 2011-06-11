import org.joda.time.DateTimeZone
import org.joda.time.DateTime
def ts = context.get("timestamp")
i = ts.indexOf("_")
if (i > -1) ts = ts.substring(0,i)
if (ts.length() > 0) {
  def timezone = context.get("user.timezone")
  def f = 1
  if (timezone.startsWith("-")) f = -1
  hr = Long.parseLong(timezone.substring(1,3))
  mn = Long.parseLong(timezone.substring(4,6)) + (hr * 60)
  sec = mn * 60
  mil = sec * 1000
  off = mil * f
  def l1 = Long.parseLong(ts,16)
  def l2 = l1 >> 10
  l3 = l2 + off
  def dt = new DateTime(l3, DateTimeZone.UTC)
  def fts = dt.toString("yyyy-MM-dd HH:mm:ss SSS")
  context.setCon("formatTimestamp",fts)
}
