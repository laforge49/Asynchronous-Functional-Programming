def newUserName = context.get("_.newUserName")
if (newUserName == "") return
def extendedContext = context.getSpecial(".extendedContext")
def updateParameters = extendedContext.updateParameters
extendedContext.assignViewParameters(context)
def userLanguage = context.get("user.language")
np1 = context.get("_.np1")
np2 = context.get("_.np2")
if (np1.length() < 6) {
  context.setCon("tooShort","true")
  return
}
if (np1 != np2) {
  context.setCon("noMatch","true")
  return
}
updateParameters.put("anotherNewUser.name",newUserName)
extendedContext.createJournalEntry(context, "another user: " + newUserName)
extendedContext.addJournalEntryRelationship(context.ACCESS_RELATIONSHIP, context.ACCESS_UUID, "reader")
def newUserUuid = extendedContext.create(context.USER_TYPE)
updateParameters.put("anotherNewUser.rolonUuid", newUserUuid)
def tagLine = context.get("_.tagLine")
extendedContext.attributeSpec(newUserUuid, "tagLine", tagLine)
extendedContext.attributeSpec(newUserUuid, "userLanguage", userLanguage)
timezone = context.get("user.timezone")
extendedContext.attributeSpec(newUserUuid, "timezone", timezone)
extendedContext.passwordSpec(newUserUuid, np1)
extendedContext.addRelationship(newUserUuid, context.PARENT_RELATIONSHIP, context.USERS_UUID, newUserName)
extendedContext.addRelationship(newUserUuid, context.ACCESS_RELATIONSHIP, newUserUuid, "owner")
extendedContext.addRelationship(newUserUuid, context.ACCESS_RELATIONSHIP, context.ACCESS_UUID, "reader")
