upd = context.get("_.upd")
if (upd == "") return
rolonUuid = context.get("rolonUuid")
extendedContext = context.getSpecial(".extendedContext")
if (!extendedContext.hasPrivilege(context, rolonUuid, "writer")) return
timestamp = context.get("timestamp")
versionId = timestamp + "|" + rolonUuid
oldTagLine = context.get(versionId + ".att.tagLine")
newTagLine = context.get("_.tagLine")
if (oldTagLine == newTagLine) return
extendedContext.assignViewParameters(context)
extendedContext.createJournalEntry(context, context.TAGLINE_NAME + ": " + newTagLine)
extendedContext.modelJournalEntryAccess(context, rolonUuid)
userUuid = context.get("user.uuid")
extendedContext.addJournalEntryRelationships(userUuid, context.TAGLINE_UUID)
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, rolonUuid, " ")
extendedContext.addJournalEntryChangeRealmRelationship(context, rolonUuid)
extendedContext.attributeSpec(rolonUuid, "tagLine", newTagLine)
realm = extendedContext.realmUuid(context, rolonUuid)
if (oldTagLine == "") {
  if (realm != null)
    extendedContext.addRelationship(rolonUuid, context.REALM_HEADLINES_RELATIONSHIP, realm, newTagLine)
  extendedContext.addRelationship(rolonUuid, context.ALL_HEADLINES_RELATIONSHIP, context.HOME_UUID, newTagLine)
} else if (newTagLine == "") {
  if (realm != null)
    extendedContext.removeRelationship(rolonUuid, context.REALM_HEADLINES_RELATIONSHIP, realm)
  extendedContext.removeRelationship(rolonUuid, context.ALL_HEADLINES_RELATIONSHIP, context.HOME_UUID)
} else {
  if (realm != null)
    extendedContext.updateRelationship(rolonUuid, context.REALM_HEADLINES_RELATIONSHIP, realm, newTagLine)
  extendedContext.updateRelationship(rolonUuid, context.ALL_HEADLINES_RELATIONSHIP, context.HOME_UUID, newTagLine)
}
updateParameters = extendedContext.updateParameters
updateParameters.put("templateRequest", "/templates/En/organizer/")
