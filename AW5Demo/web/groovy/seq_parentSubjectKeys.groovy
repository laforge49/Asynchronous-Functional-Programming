def extendedContext = context.getSpecial(".extendedContext")
def loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix+"."
context.setCon(loopPrefix + "relType", "parent")
def parentSubjectKeysSequence = extendedContext.groovySequence(context,"subjectKeys")
context.setSpecial(loopPrefix+"sequence",parentSubjectKeysSequence)
context.setCon(loopPrefix+"isUuidSequence","true")
