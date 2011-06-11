rolonUuid = context.get("rolonUuid")
extendedContext = context.getSpecial(".extendedContext")
if (!extendedContext.hasPrivilege(context, rolonUuid, "owner")) return
userUuid = context.get("user.uuid")
if (rolonUuid == userUuid) return
if (rolonUuid == context.ADMIN_GROUP_NAME) rturn
np1 = context.get("_.np1")
np2 = context.get("_.np2")
if (np1 == "" && np2 == "") return
if (np1.length() < 6 || np2.length() < 6) {
  context.setCon("tooShort","true")
  return
}
if (np1 != np2) {
  context.setCon("noMatch","true")
  return
}
def extendedContext = context.getSpecial(".extendedContext")
def updateParameters = extendedContext.updateParameters
extendedContext.createJournalEntry(context, context.PASSWORD_NAME)
extendedContext.addJournalEntryRelationships(rolonUuid, context.PASSWORD_UUID)
extendedContext.addJournalEntryRelationship(context.ACCESS_RELATIONSHIP, rolonUuid, "reader")
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, rolonUuid, " ")
extendedContext.assignViewParameters(context)
extendedContext.passwordSpec(rolonUuid, np1)
updateParameters.put("updated","true")
updateParameters.put("templateRequest", "/templates/En/organizer/")
