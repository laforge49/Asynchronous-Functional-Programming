loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix + "."
uuid = context.get(loopPrefix + "uuid")
extendedContext = context.getSpecial(".extendedContext")
i = uuid.indexOf("_")
roleName = uuid.substring(0,i)
role = extendedContext.role(context.typeName(roleName))
if (role != null && role.isA(context.GROUP_TYPE) && extendedContext.hasPrivilege(context, uuid, "writer"))
  context.setCon(loopPrefix + "ignore", "")
else
  context.setCon(loopPrefix + "ignore", "true")
