def extendedContext = context.getSpecial(".extendedContext")
def loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix+"."
context.setCon(loopPrefix + "relType", "parent")
def parentObjectsSequence = extendedContext.groovySequence(context,"objects")
context.setSpecial(loopPrefix+"sequence",parentObjectsSequence)
context.setCon(loopPrefix+"isUuidSequence","true")
