def extendedContext = context.getSpecial(".extendedContext")
def loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix+"."
def timestamp = context.get("timestamp")
def rolonUuid = context.get(loopPrefix+"rolonUuid")
def subjectTypesSequence = extendedContext.subjectTypesSequence(timestamp,rolonUuid)
context.setSpecial(loopPrefix+"sequence",subjectTypesSequence)
