def extendedContext = context.getSpecial(".extendedContext")
def contextNames = context.stringSet()
def sequence = extendedContext.navigableSequence(contextNames,false)
def loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix+"."
context.setSpecial(loopPrefix+"sequence",sequence)
