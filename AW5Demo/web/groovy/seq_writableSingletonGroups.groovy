extendedContext = context.getSpecial(".extendedContext")
loopPrefix = context.get("loopPrefix")
if (loopPrefix != "") loopPrefix += "."
context.setCon(loopPrefix+"relType","parent")
subjectKeysSequence = extendedContext.groovySequence(context, "subjectKeys")
seq = extendedContext.contextFilterSequence(context,subjectKeysSequence,"writableSingletonGroup")
context.setSpecial(loopPrefix+"sequence",seq)
context.setCon(loopPrefix+"isUuidSequence","true")
