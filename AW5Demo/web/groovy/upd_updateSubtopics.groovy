upd = context.get("_.upd")
if (upd == "") return
userUuid = context.get("user.uuid")
extendedContext = context.getSpecial(".extendedContext")
subjUuid = context.get("_.rolonUuid")
if (!extendedContext.hasPrivilege(context, subjUuid, "writer")) return
objUuid = context.get("_.uuid")
if (!extendedContext.hasPrivilege(context, subjUuid, "owner")) return
oldValue = extendedContext.getValue(context, subjUuid, context.SUBTOPIC_RELATIONSHIP, objUuid)
if (oldValue == null) oldValue = "none"
def newValue = ""
if (context.contains("_.remove")) newValue = "remove"
else if (context.contains("_.add")) newValue = "add"
else return
if (oldValue == "none" && newValue == "remove") return
if (oldValue != "none" && newValue == "add") return
extendedContext.assignViewParameters(context)
extendedContext.createJournalEntry(context, context.EDIT_SUBTOPICS_NAME + ": " + newValue)
extendedContext.modelJournalEntryAccess(context, subjUuid)
extendedContext.addJournalEntryRelationships(userUuid, context.EDIT_SUBTOPICS_UUID)
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, subjUuid, " ")
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, objUuid, " ")
extendedContext.addJournalEntryChangeRealmRelationship(context, subjUuid)
if (newValue == "add") {
  extendedContext.addRelationship(subjUuid, context.SUBTOPIC_RELATIONSHIP, objUuid, " ")
} else {
  extendedContext.removeRelationship(subjUuid, context.SUBTOPIC_RELATIONSHIP, objUuid)
}

