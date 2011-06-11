upd = context.get("_.upd")
if (upd == "") return
rolonUuid = context.get("rolonUuid")
extendedContext = context.getSpecial(".extendedContext")
if (!extendedContext.hasPrivilege(context, rolonUuid, "writer")) return
timestamp = context.get("timestamp")
versionId = timestamp + "|" + rolonUuid
oldText = context.get(versionId + ".att.text")
newText = context.get("_.text")
if (oldText == newText) return
extendedContext.assignViewParameters(context)
extendedContext.createJournalEntry(context, context.EDIT_TEXT_NAME)
extendedContext.modelJournalEntryAccess(context, rolonUuid)
userUuid = context.get("user.uuid")
extendedContext.addJournalEntryRelationships(userUuid, context.EDIT_TEXT_UUID)
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, rolonUuid, " ")
extendedContext.addJournalEntryChangeRealmRelationship(context, rolonUuid)
extendedContext.attributeSpec(rolonUuid, "text", newText)
updateParameters = extendedContext.updateParameters
updateParameters.put("templateRequest", "/templates/En/organizer/")
updateParameters.put("newText", newText)
