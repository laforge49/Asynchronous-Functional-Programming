def newGroupName = context.get("_.groupName")
if (newGroupName == "") return
rolonUuid = context.get("rolonUuid")
def extendedContext = context.getSpecial(".extendedContext")
if (!extendedContext.hasPrivilege(context, rolonUuid, "writer")) return
def tagLine = context.get("_.tagLine")
def updateParameters = extendedContext.updateParameters
userUuid = context.get("user.uuid")
extendedContext.assignViewParameters(context)
extendedContext.createJournalEntry(context, context.CREATE_MY_GROUP_NAME + ": " + newGroupName)
extendedContext.modelJournalEntryAccess(context, rolonUuid)
extendedContext.addJournalEntryRelationships(userUuid, context.CREATE_MY_GROUP_UUID)
extendedContext.addJournalEntryChangeRealmRelationship(context, rolonUuid)
def newGroupUuid = extendedContext.create(context.MY_GROUP_TYPE)
extendedContext.addJournalEntryRelationship(context.ACCESS_RELATIONSHIP, newGroupUuid, "reader")
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, rolonUuid, " ")
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, newGroupUuid, " ")
extendedContext.addRelationship(newGroupUuid, context.PARENT_RELATIONSHIP, rolonUuid, newGroupName)
realm = extendedContext.realmUuid(context, rolonUuid)
extendedContext.addRelationship(newGroupUuid, context.ACCESS_RELATIONSHIP, realm, "owner")
extendedContext.addRelationship(newGroupUuid, context.REALM_RELATIONSHIP, realm, " ")
if (tagLine.length() > 0) {
  extendedContext.addRelationship(newGroupUuid, context.REALM_HEADLINES_RELATIONSHIP, realm, tagLine)
  extendedContext.addRelationship(newGroupUuid, context.ALL_HEADLINES_RELATIONSHIP, context.HOME_UUID, tagLine)
  extendedContext.attributeSpec(newGroupUuid, "tagLine", tagLine)
}
updateParameters.put("templateRequest", "/templates/En/organizer/")
updateParameters.put("_.rolonUuid",newGroupUuid)
