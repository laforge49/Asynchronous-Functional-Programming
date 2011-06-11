def loopPrefix = context.get("loopPrefix")
if(!loopPrefix.equals(""))
  loopPrefix += "."
def key = context.get(loopPrefix + "key")
def postfix = context.get(loopPrefix + "postfix")
if (postfix.length() > 0 && !key.endsWith("." + postfix))
  context.setVar(loopPrefix + "ignore", "true")