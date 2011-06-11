def extendedContext = context.getSpecial(".extendedContext")
def loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix+"."
def timestamp = context.get("timestamp")
def rolonUuid = context.get(loopPrefix+"rolonUuid")
def relType = context.relationshipName(context.get(loopPrefix+"relType"))
def subjectValuesSequence = extendedContext.subjectValuesSequence(timestamp,rolonUuid, relType)
context.setSpecial(loopPrefix+"sequence",subjectValuesSequence)
