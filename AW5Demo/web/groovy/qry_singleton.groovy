uuid = context.get("rolonUuid")
i = uuid.indexOf("_")
extendedContext = context.getSpecial(".extendedContext")
roleName = uuid.substring(0,i)
if (extendedContext.role(context.typeName(roleName)) != null)
  context.setCon("singleton", "true")
else
  context.setCon("singleton", "")
