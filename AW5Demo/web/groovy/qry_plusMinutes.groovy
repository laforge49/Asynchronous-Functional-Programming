import org.joda.time.DateTime
import org.joda.time.DateTimeZone

def ts = context.get("timestamp")
i = ts.indexOf("_")
if (i > -1) ts = ts.substring(0,i)
def l1 = Long.parseLong(ts,16)
def l2 = l1 >> 10
def dt = new DateTime(l2, DateTimeZone.UTC)
def minutes = context.get("minutes")
def offset = Integer.parseInt(minutes)
def ndt = dt.plusMinutes(offset)
def mills = ndt.getMillis()
def rational = (mills << 10) + 1023
def nts = Long.toHexString(rational)
context.setCon("timestamp", nts)
