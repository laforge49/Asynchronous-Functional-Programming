def extendedContext = context.getSpecial(".extendedContext")
def propertyNames = extendedContext.propertyNames()
def sequence = extendedContext.navigableSequence(propertyNames,false)
def loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix+"."
context.setSpecial(loopPrefix+"sequence",sequence)
