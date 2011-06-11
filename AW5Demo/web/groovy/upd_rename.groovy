rolonUuid = context.get("rolonUuid")
newName = context.get("_.newName")
if (newName == "") return
extendedContext = context.getSpecial(".extendedContext")
if (!extendedContext.hasPrivilege(context, rolonUuid, "writer")) return
i = rolonUuid.indexOf("_")
roleName = rolonUuid.substring(0,i)
if (extendedContext.role(context.typeName(roleName)) != null) return
timestamp = context.get("timestamp")
versionId = timestamp + "|" + rolonUuid
oldName = context.get(versionId + ".name")
if (oldName == newName) return
extendedContext.assignViewParameters(context)
extendedContext.createJournalEntry(context, context.RENAME_NAME + ": " + oldName + " -> " + newName)
extendedContext.modelJournalEntryAccess(context, rolonUuid)
userUuid = context.get("user.uuid")
extendedContext.addJournalEntryRelationships(userUuid, context.RENAME_UUID)
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, rolonUuid, " ")
extendedContext.addJournalEntryChangeRealmRelationship(context, rolonUuid)
relationships = context.getSpecial(versionId + ".relationships")
parents = relationships.get(context.PARENT_RELATIONSHIP)
parentUuid = parents.uuid(0)
extendedContext.updateRelationship(rolonUuid, context.PARENT_RELATIONSHIP, parentUuid, newName)
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, parentUuid, " ")
updateParameters = extendedContext.updateParameters
updateParameters.put("templateRequest", "/templates/En/organizer/")
