def extendedContext = context.getSpecial(".extendedContext")
def emptySequence = extendedContext.emptySequence()
def loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix+"."
context.setSpecial(loopPrefix+"sequence",emptySequence)
