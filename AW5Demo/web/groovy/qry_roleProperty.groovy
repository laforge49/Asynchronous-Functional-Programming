def extendedContext = context.getSpecial(".extendedContext")
def roleName = context.get("role")
def role = extendedContext.role(context.typeName(roleName))
def pname = context.get("pname")
def name = context.get("name")
def value = role.property(pname)
if (value == null) {
  context.setVar(name,value)
}
