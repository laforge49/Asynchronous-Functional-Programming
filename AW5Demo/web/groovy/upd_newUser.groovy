def newUserName = context.get("_.newUserName")
if (newUserName == "") return
def extendedContext = context.getSpecial(".extendedContext")
if (!extendedContext.hasPrivilege(context, context.NEW_USER_UUID, "reader")) return
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
updateParameters.put("newUser.name",newUserName)
def newUserUuid = extendedContext.generateUuid(context.USER_TYPE)
context.setCon("user.uuid", newUserUuid)
updateParameters.put("newUser.rolonUuid", newUserUuid)
updateParameters.put("_.rolonUuid",newUserUuid)
extendedContext.createJournalEntry(context, context.NEW_USER_NAME + ": " + newUserName)
extendedContext.create()
extendedContext.addJournalEntryRelationships(newUserUuid, context.NEW_USER_UUID)
extendedContext.addJournalEntryRelationship(context.ACCESS_RELATIONSHIP, context.ACCESS_UUID, "reader")
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, newUserUuid, " ")
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, context.USERS_UUID, " ")
extendedContext.addJournalEntryRelationship(context.REALM_CHANGE_RELATIONSHIP, context.HOME_UUID, " ")
def tagLine = context.get("_.tagLine")
extendedContext.attributeSpec(newUserUuid, "tagLine", tagLine)
extendedContext.attributeSpec(newUserUuid, "userLanguage", userLanguage)
timezone = context.get("user.timezone")
extendedContext.attributeSpec(newUserUuid, "timezone", timezone)
extendedContext.passwordSpec(newUserUuid, np1)
extendedContext.addRelationship(newUserUuid, context.PARENT_RELATIONSHIP, context.USERS_UUID, newUserName)
extendedContext.addRelationship(newUserUuid, context.ACCESS_RELATIONSHIP, newUserUuid, "owner")
extendedContext.addRelationship(newUserUuid, context.ACCESS_RELATIONSHIP, context.ACCESS_UUID, "reader")
extendedContext.addRelationship(newUserUuid, context.REALM_RELATIONSHIP, context.HOME_UUID, " ")
if (tagLine.length() > 0) {
  extendedContext.addRelationship(newUserUuid, context.REALM_HEADLINES_RELATIONSHIP, context.HOME_UUID, tagLine)
  extendedContext.addRelationship(newUserUuid, context.ALL_HEADLINES_RELATIONSHIP, context.HOME_UUID, tagLine)
}
updateParameters.put("templateRequest", "/templates/En/organizer/")
