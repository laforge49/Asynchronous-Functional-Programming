loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix + "."
uuid = context.get(loopPrefix + "uuid")
i = uuid.indexOf("_")
extendedContext = context.getSpecial(".extendedContext")
roleName = uuid.substring(0,i)
if (extendedContext.role(context.typeName(roleName)) != null)
  context.setCon(loopPrefix + "ignore", "")
else
  context.setCon(loopPrefix + "ignore", "true")
