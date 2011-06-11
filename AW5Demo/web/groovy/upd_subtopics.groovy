upd = context.get("_.upd")
if (upd == "") return
rolonUuid = context.get("rolonUuid")
extendedContext = context.getSpecial(".extendedContext")
if (!extendedContext.hasPrivilege(context, rolonUuid, "writer")) return
timestamp = context.get("timestamp")
versionId = timestamp + "|" + rolonUuid
extendedContext.assignViewParameters(context)
extendedContext.createJournalEntry(context, context.CREATE_SUBTOPICS_NAME)
userUuid = context.get("user.uuid")
extendedContext.addJournalEntryRelationships(userUuid, context.CREATE_SUBTOPICS_UUID)
def subtopicsUuid = extendedContext.create(context.SUBTOPICS_TYPE)
extendedContext.addJournalEntryRelationship(context.ACCESS_RELATIONSHIP, context.ACCESS_UUID, "reader")
extendedContext.addJournalEntryRelationship(context.REALM_CHANGE_RELATIONSHIP, rolonUuid, " ")
extendedContext.addRelationship(subtopicsUuid, context.PARENT_RELATIONSHIP, rolonUuid, context.SUBTOPICS_NAME)
def updateParameters = extendedContext.updateParameters
updateParameters.put("_.rolonUuid",subtopicsUuid)
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, rolonUuid, " ")
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, subtopicsUuid, " ")
extendedContext.addRelationship(subtopicsUuid, context.ACCESS_RELATIONSHIP, rolonUuid, "writer")
extendedContext.addRelationship(subtopicsUuid, context.ACCESS_RELATIONSHIP, context.ACCESS_UUID, "reader")
extendedContext.addRelationship(subtopicsUuid, context.REALM_RELATIONSHIP, rolonUuid, " ")
updateParameters.put("templateRequest", "/templates/En/organizer/")
