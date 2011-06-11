rolonUuid = context.get("_.rolonUuid")
upd = context.get("_.upd")
if (upd == "") return
extendedContext = context.getSpecial(".extendedContext")
if (!extendedContext.hasPrivilege(context, rolonUuid, "writer")) return
timestamp = context.get("timestamp")
versionId = timestamp + "|" + rolonUuid
name = context.get(versionId + ".name")
extendedContext.assignViewParameters(context)
def updateParameters = extendedContext.updateParameters
userUuid = context.get("user.uuid")
if (userUuid == rolonUuid) {
  userUuid = context.SYSTEM_USER_UUID
  context.setCon("user.uuid", userUuid)
  updateParameters.put("newUser.rolonUuid",context.ANONYMOUS_UUID)
}
extendedContext.createJournalEntry(context, context.DELETE_NAME + ": " + name)
extendedContext.modelJournalEntryAccess(context, rolonUuid, rolonUuid)
updateParameters.put("templateRequest", "/templates/En/organizer/")
extendedContext.addJournalEntryRelationships(userUuid, context.DELETE_UUID)
extendedContext.addJournalEntryChangeRealmRelationship(context, rolonUuid)
relationships = context.getSpecial(versionId + ".relationships")
parents = relationships.get(context.PARENT_RELATIONSHIP)
size = parents.list.size()
i = 0
while (i < size) {
  extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, parents.uuid(i), " ")
  i += 1
}
parentUuid = parents.uuid(0)
updateParameters.put("_.rolonUuid", parentUuid)
extendedContext.delete(rolonUuid)
extendedContext.removeRelationship(rolonUuid, context.PARENT_RELATIONSHIP, parentUuid)
