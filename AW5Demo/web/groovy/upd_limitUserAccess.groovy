upd = context.get("_.upd")
if (upd == "") return
objUuid = context.get("_.uuid")
subjUuid = context.get("_.rolonUuid")
extendedContext = context.getSpecial(".extendedContext")
if (!extendedContext.hasPrivilege(context, subjUuid, "owner")) return
oldValue = extendedContext.getValue(context, subjUuid, context.LIMIT_RELATIONSHIP, objUuid)
if (oldValue == null) oldValue = "owner"
newValue = context.get("_.limit")
if (oldValue == newValue) return
if (newValue != "none" && newValue != "reader" && newValue != "writer" && newValue != "owner") return
extendedContext.assignViewParameters(context)
extendedContext.createJournalEntry(context, context.EDIT_USER_LIMITS_NAME + ": " + oldValue + "->" + newValue)
extendedContext.addJournalEntryRelationship(context.ACCESS_RELATIONSHIP, objUuid, "reader")
extendedContext.modelJournalEntryAccess(context, subjUuid)
userUuid = context.get("user.uuid")
extendedContext.addJournalEntryRelationships(userUuid, context.EDIT_USER_LIMITS_UUID)
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, subjUuid, " ")
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, objUuid, " ")
extendedContext.addJournalEntryChangeRealmRelationship(context, subjUuid)
if (oldValue == "owner") {
  extendedContext.addRelationship(subjUuid, context.LIMIT_RELATIONSHIP, objUuid, newValue)
} else if (newValue == "owner") {
  extendedContext.removeRelationship(subjUuid, context.LIMIT_RELATIONSHIP, objUuid)
} else {
  extendedContext.updateRelationship(subjUuid, context.LIMIT_RELATIONSHIP, objUuid, newValue)
}
