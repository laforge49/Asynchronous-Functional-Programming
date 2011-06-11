def extendedContext = context.getSpecial(".extendedContext")
def updateParameters = extendedContext.updateParameters
def currentRequest = context.get("_.currentRequest")
def language = context.get("_.language")
updateParameters.put("templateRequest",currentRequest)
updateParameters.put("user.language",language)
def userUuid = context.get("user.uuid")
extendedContext.createJournalEntry(context, "set language: " + language)
extendedContext.addJournalEntryRelationship(context.ACCESS_RELATIONSHIP, context.ACCESS_UUID, "reader")
if (userUuid != context.ANONYMOUS_UUID) {
  extendedContext.attributeSpec(userUuid, "language", language)
}
