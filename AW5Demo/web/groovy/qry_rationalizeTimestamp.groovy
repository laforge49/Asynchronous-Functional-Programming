def ts = context.get("timestamp")
i = ts.indexOf("_")
if (i > -1) ts = ts.substring(0,i)
def i1 = Long.parseLong(ts,16) >> 10
def rational = (i1 << 10) + 1023
def nts = Long.toHexString(rational)
context.setCon("timestamp", nts)
