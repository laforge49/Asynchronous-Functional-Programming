upd = context.get("_.upd")
if (upd == "") return
subjUuid = context.get("_.uuid")
objUuid = context.get("_.rolonUuid")
extendedContext = context.getSpecial(".extendedContext")
if (!extendedContext.hasPrivilege(context, objUuid, "writer")) return
oldValue = extendedContext.getValue(context, subjUuid, context.MEMBER_RELATIONSHIP, objUuid)
if (oldValue == null) oldValue = "none"
newValue = context.get("_.membership")
if (oldValue == newValue) return
if (newValue != "none" && newValue != "reader" && newValue != "writer" && newValue != "owner") return
extendedContext.assignViewParameters(context)
extendedContext.createJournalEntry(context, context.EDIT_MEMBERSHIP_NAME + ": " + oldValue + "->" + newValue)
extendedContext.addJournalEntryRelationship(context.ACCESS_RELATIONSHIP, subjUuid, "reader")
extendedContext.modelJournalEntryAccess(context, objUuid)
userUuid = context.get("user.uuid")
extendedContext.addJournalEntryRelationships(userUuid, context.EDIT_MEMBERSHIP_UUID)
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, subjUuid, " ")
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, objUuid, " ")
extendedContext.addJournalEntryChangeRealmRelationship(context, objUuid)
if (oldValue == "none") {
  extendedContext.addRelationship(subjUuid, context.MEMBER_RELATIONSHIP, objUuid, newValue)
} else if (newValue == "none") {
  extendedContext.removeRelationship(subjUuid, context.MEMBER_RELATIONSHIP, objUuid)
} else {
  extendedContext.updateRelationship(subjUuid, context.MEMBER_RELATIONSHIP, objUuid, newValue)
}
