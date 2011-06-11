upd = context.get("_.upd")
if (upd == "") return
objUuid = context.get("_.uuid")
subjUuid = context.get("_.rolonUuid")
extendedContext = context.getSpecial(".extendedContext")
if (!extendedContext.hasPrivilege(context, subjUuid, "owner")) return
oldValue = extendedContext.getValue(context, subjUuid, context.ACCESS_RELATIONSHIP, objUuid)
if (oldValue == null) oldValue = "none"
newValue = context.get("_.access")
if (oldValue == newValue) return
if (newValue != "none" && newValue != "reader" && newValue != "writer" && newValue != "owner") return
extendedContext.assignViewParameters(context)
extendedContext.createJournalEntry(context, context.EDIT_GROUP_ACCESS_NAME + ": " + oldValue + "->" + newValue)
extendedContext.addJournalEntryRelationship(context.ACCESS_RELATIONSHIP, objUuid, "reader")
extendedContext.modelJournalEntryAccess(context, subjUuid)
userUuid = context.get("user.uuid")
extendedContext.addJournalEntryRelationships(userUuid, context.EDIT_GROUP_ACCESS_UUID)
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, subjUuid, " ")
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, objUuid, " ")
extendedContext.addJournalEntryChangeRealmRelationship(context, subjUuid)
if (oldValue == "none") {
  extendedContext.addRelationship(subjUuid, context.ACCESS_RELATIONSHIP, objUuid, newValue)
} else if (newValue == "none") {
  extendedContext.removeRelationship(subjUuid, context.ACCESS_RELATIONSHIP, objUuid)
} else {
  extendedContext.updateRelationship(subjUuid, context.ACCESS_RELATIONSHIP, objUuid, newValue)
}
