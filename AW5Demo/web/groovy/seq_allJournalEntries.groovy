def extendedContext = context.getSpecial(".extendedContext")
def loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix+"."
def sequence = extendedContext.journalEntryTimestampSequence(context)
sequence = extendedContext.invertedTimestampSequence(sequence)
context.setCon(loopPrefix+"isUuidSequence","true")
context.setSpecial(loopPrefix+"sequence",sequence)
