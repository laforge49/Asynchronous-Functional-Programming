def extendedContext = context.getSpecial(".extendedContext")
def role1Name = context.get("role1")
def role2Name = context.get("role2")
if (!role1Name.equals("")) {
  def role1 = extendedContext.role(context.typeName(role1Name))
  if (role1 != null) {
    if (role1.isA(context.typeName(role2Name)))
      context.setCon("isSubRole", "true")
    else
      context.setCon("isSubRole", "")
  }
}
