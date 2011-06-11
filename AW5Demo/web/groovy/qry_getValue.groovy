def extendedContext = context.getSpecial(".extendedContext")
def loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix+"."
def treeMap = context.getSpecial(loopPrefix+"treeMap")
def key = context.get(loopPrefix+"key")
def value = treeMap.get(key)
context.setSpecial(loopPrefix+"value",value)
