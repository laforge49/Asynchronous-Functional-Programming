def subtopicName = context.get("_.subtopicName")
if (subtopicName == "") return
rolonUuid = context.get("rolonUuid")
def extendedContext = context.getSpecial(".extendedContext")
if (!extendedContext.hasPrivilege(context, rolonUuid, "writer")) return
def tagLine = context.get("_.tagLine")
def updateParameters = extendedContext.updateParameters
userUuid = context.get("user.uuid")
extendedContext.assignViewParameters(context)
extendedContext.createJournalEntry(context, context.CREATE_SUBTOPIC_NAME + ": " + subtopicName)
extendedContext.addJournalEntryRelationships(userUuid, context.CREATE_SUBTOPIC_UUID)
extendedContext.addJournalEntryChangeRealmRelationship(context, rolonUuid)
def subtopicUuid = extendedContext.create(context.SUBTOPIC_TYPE)
extendedContext.addJournalEntryRelationship(context.ACCESS_RELATIONSHIP, userUuid, "reader")
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, rolonUuid, " ")
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, subtopicUuid, " ")
extendedContext.addRelationship(subtopicUuid, context.PARENT_RELATIONSHIP, rolonUuid, subtopicName)
realm = extendedContext.realmUuid(context, rolonUuid)
if (realm.endsWith("_"+context.GROUP_TYPE))
  extendedContext.addRelationship(subtopicUuid, context.ACCESS_RELATIONSHIP, userUuid, "owner")
else
  extendedContext.addRelationship(subtopicUuid, context.ACCESS_RELATIONSHIP, realm, "owner")
extendedContext.addRelationship(subtopicUuid, context.REALM_RELATIONSHIP, realm, " ")
if (tagLine.length() > 0) {
  extendedContext.addRelationship(subtopicUuid, context.REALM_HEADLINES_RELATIONSHIP, realm, tagLine)
  extendedContext.addRelationship(subtopicUuid, context.ALL_HEADLINES_RELATIONSHIP, context.HOME_UUID, tagLine)
  extendedContext.attributeSpec(subtopicUuid, "tagLine", tagLine)
}
updateParameters.put("templateRequest", "/templates/En/organizer/")
updateParameters.put("_.rolonUuid",subtopicUuid)
