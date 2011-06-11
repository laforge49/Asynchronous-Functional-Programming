extendedContext = context.getSpecial(".extendedContext")
loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix + "."
uuid = context.get(loopPrefix + "uuid")
privilege = context.get(loopPrefix + "privilege")
if (extendedContext.hasPrivilege(context, uuid, privilege))
  context.setCon(loopPrefix + "ignore", "")
else
  context.setCon(loopPrefix + "ignore", "true")
