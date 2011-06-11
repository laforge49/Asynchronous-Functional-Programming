def extendedContext = context.getSpecial(".extendedContext")
def loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix+"."
def rolonUuid = context.get(loopPrefix+"rolonUuid")
def timestamp = context.get("timestamp")
def relationships = context.getSpecial(timestamp + "|" + rolonUuid+".relationships")
def objectTypesSequence = extendedContext.objectTypesSequence(relationships)
context.setSpecial(loopPrefix+"sequence",objectTypesSequence)
