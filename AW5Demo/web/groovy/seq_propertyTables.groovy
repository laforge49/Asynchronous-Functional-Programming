def extendedContext = context.getSpecial(".extendedContext")
def propertyNames = extendedContext.propertyNames()
def propertiesSequence = extendedContext.navigableSequence(propertyNames,false)
def tablesSequence = extendedContext.prefixesSequence(propertiesSequence,(char)'.')
def loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix+"."
context.setSpecial(loopPrefix+"sequence",tablesSequence)
