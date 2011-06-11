def extendedContext = context.getSpecial(".extendedContext")
def loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix+"."
def timestamp = context.get("timestamp")
def rolonUuid = context.get(loopPrefix+"rolonUuid")
versionId = timestamp + "|" + rolonUuid
def effectedRolons = context.getSpecial(versionId + ".effectedRolons")
sequence = extendedContext.keySequence(effectedRolons)
context.setSpecial(loopPrefix+"sequence",sequence)
context.setCon(loopPrefix+"isUuidSequence","true")
