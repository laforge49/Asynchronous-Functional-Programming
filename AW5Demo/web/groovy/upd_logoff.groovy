def extendedContext = context.getSpecial(".extendedContext")
def updateParameters = extendedContext.updateParameters
def oldRequest = context.get("_.oldRequest")
updateParameters.put("templateRequest",oldRequest)
oldUuid = context.get("_.rolonUuid")
updateParameters.put("_.rolonUuid",oldUuid)
updateParameters.put("newUser.rolonUuid",context.ANONYMOUS_UUID)
userUuid = context.get("user.uuid")
extendedContext.createJournalEntry(context, context.LOGOFF_NAME)
extendedContext.addJournalEntryRelationships(userUuid, context.LOGOFF_UUID)
extendedContext.addJournalEntryRelationship(context.ACCESS_RELATIONSHIP, userUuid, "reader")
past = context.get("_.past")
updateParameters.put("_.past",past)
updateParameters.put("_.timestamp",past)
