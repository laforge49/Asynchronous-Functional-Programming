extendedContext = context.getSpecial(".extendedContext")
typ = context.get("type")
exp = extendedContext.expandTypeName(typ)
context.setCon("expandedType",exp)