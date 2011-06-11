upd = context.get("_.upd")
if (upd == "") return
rolonUuid = context.get("rolonUuid")
extendedContext = context.getSpecial(".extendedContext")
if (!extendedContext.hasPrivilege(context, rolonUuid, "writer")) return
timestamp = context.get("timestamp")
versionId = timestamp + "|" + rolonUuid
extendedContext.assignViewParameters(context)
extendedContext.createJournalEntry(context, context.BASE_GROUPS_NAME)
userUuid = context.get("user.uuid")
extendedContext.addJournalEntryRelationships(userUuid, context.GROUPS_UUID)
def groupsUuid = extendedContext.create(context.BASE_GROUPS_TYPE)
extendedContext.addJournalEntryRelationship(context.ACCESS_RELATIONSHIP, context.ACCESS_UUID, "reader")
extendedContext.addJournalEntryRelationship(context.REALM_CHANGE_RELATIONSHIP, rolonUuid, " ")
extendedContext.modelAccess(context, groupsUuid, rolonUuid)
extendedContext.addRelationship(groupsUuid, context.PARENT_RELATIONSHIP, rolonUuid, context.BASE_GROUPS_NAME)
def updateParameters = extendedContext.updateParameters
updateParameters.put("_.rolonUuid",groupsUuid)
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, rolonUuid, " ")
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, groupsUuid, " ")
extendedContext.addRelationship(groupsUuid, context.REALM_RELATIONSHIP, rolonUuid, " ")
updateParameters.put("templateRequest", "/templates/En/organizer/")
