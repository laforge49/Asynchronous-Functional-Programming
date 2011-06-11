def loopPrefix = context.get("loopPrefix")
if(!loopPrefix.equals(""))
  loopPrefix += "."
def key = context.get(loopPrefix + "key")
def text = context.get(loopPrefix + "text")
if (!key.contains(text))
  context.setVar(loopPrefix + "ignore", "true")