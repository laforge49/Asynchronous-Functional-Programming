def extendedContext = context.getSpecial(".extendedContext")
def updateParameters = extendedContext.updateParameters
def tagLine = context.get("_.tagLine")
def newGroupName = context.get("_.groupName")
if (newGroupName == "") return
rolonUuid = context.get("rolonUuid")
userUuid = context.get("user.uuid")
extendedContext.assignViewParameters(context)
extendedContext.createJournalEntry(context, context.CREATE_GROUP_NAME + ": " + newGroupName)
extendedContext.modelJournalEntryAccess(context, rolonUuid)
extendedContext.addJournalEntryRelationships(userUuid, context.CREATE_GROUP_UUID)
extendedContext.addJournalEntryChangeRealmRelationship(context, rolonUuid)
def newGroupUuid = extendedContext.create(context.GROUP_TYPE)
extendedContext.addJournalEntryRelationship(context.ACCESS_RELATIONSHIP, context.ACCESS_UUID, "reader")
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, rolonUuid, " ")
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, newGroupUuid, " ")
extendedContext.addRelationship(newGroupUuid, context.PARENT_RELATIONSHIP, rolonUuid, newGroupName)
extendedContext.modelAccess(context, newGroupUuid, rolonUuid)
extendedContext.addRelationship(newGroupUuid, context.ACCESS_RELATIONSHIP, userUuid, "owner")
realm = extendedContext.realmUuid(context, rolonUuid)
extendedContext.addRelationship(newGroupUuid, context.REALM_RELATIONSHIP, realm, " ")
if (tagLine.length() > 0) {
  extendedContext.addRelationship(newGroupUuid, context.REALM_HEADLINES_RELATIONSHIP, realm, tagLine)
  extendedContext.addRelationship(newGroupUuid, context.ALL_HEADLINES_RELATIONSHIP, context.HOME_UUID, tagLine)
  extendedContext.attributeSpec(newGroupUuid, "tagLine", tagLine)
}
updateParameters.put("templateRequest", "/templates/En/organizer/")
updateParameters.put("_.rolonUuid",newGroupUuid)
