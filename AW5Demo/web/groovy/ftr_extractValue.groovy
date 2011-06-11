def extendedContext = context.getSpecial(".extendedContext")
def loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix+"."
def key = context.get(loopPrefix+"key")
def i = key.indexOf(5)
def value = key.substring(i+1)
context.setCon(loopPrefix+"value",value)
