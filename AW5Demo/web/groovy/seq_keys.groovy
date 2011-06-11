def extendedContext = context.getSpecial(".extendedContext")
def loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix+"."
def treeMap = context.getSpecial(loopPrefix+"treeMap")
def sequence = extendedContext.keySequence(treeMap)
context.setSpecial(loopPrefix+"sequence",sequence)
