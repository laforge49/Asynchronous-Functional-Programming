userUuid = context.get("user.uuid")
extendedContext = context.getSpecial(".extendedContext")
rolonUuid = context.get("rolonUuid")
if (!extendedContext.hasPrivilege(context, rolonUuid, "writer")) return
rel = context.get("_.rel")
objUuid = context.get("_.objUuid")
order = context.get("_.order")
irel = context.relationshipName(rel)
if (irel == null) return
timestamp = context.get("timestamp")
versionId = timestamp + "|" + rolonUuid
relationships = context.getSpecial(versionId + ".relationships")
if (relationships == null) return
objectIdentifiers = relationships.get(irel)
if (objectIdentifiers == null) return
if (!objectIdentifiers.map.containsKey(objUuid)) return
i = objectIdentifiers.indexOf(objUuid)
s = objectIdentifiers.map.size()
j = i
uuid = ""
before = true
if (order == "Top") {
  if (i == 0) return
  j = 0
  uuid = objectIdentifiers.uuid(j)
} else if (order == "Up") {
  if (i > 0) j = i - 1
  else return
  uuid = objectIdentifiers.uuid(j)
} else if (order == "Down") {
  if (i + 1 < s) j = i + 1
  else return
  uuid = objectIdentifiers.uuid(j)
  before = false
} else if (order == "Bottom") {
  j = s - 1
  if (i == j) return
  uuid = objectIdentifiers.uuid(j)
  before = false
} else return
updateParameters = extendedContext.updateParameters
updateParameters.put("_.rel",rel)
extendedContext.assignViewParameters(context)
extendedContext.createJournalEntry(context, context.ORDER_NAME)
extendedContext.modelJournalEntryAccess(context, rolonUuid)
extendedContext.addJournalEntryRelationships(userUuid, context.ORDER_UUID)
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, rolonUuid, " ")
extendedContext.addJournalEntryChangeRealmRelationship(context, rolonUuid)
if (before) {
  extendedContext.beforeSpec(rolonUuid, irel, objUuid, uuid)
} else {
  extendedContext.afterSpec(rolonUuid, irel, objUuid, uuid)
}