def extendedContext = context.getSpecial(".extendedContext")
def contextNames = context.stringSet()
def contextSequence = extendedContext.navigableSequence(contextNames,false)
def loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix+"."
def rolonUuid = context.get(loopPrefix+"rolonUuid")
def timestamp = context.get("timestamp")
def prefix = timestamp + "|" + rolonUuid + ".att."
def attributesSequence = extendedContext.subSequence(contextSequence,prefix)
context.setSpecial(loopPrefix+"sequence",attributesSequence)
