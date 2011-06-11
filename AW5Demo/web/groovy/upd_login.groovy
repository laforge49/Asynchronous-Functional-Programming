def rolonUuid = context.get("rolonUuid")
if (rolonUuid.startsWith("systemUser_")) {
  context.setCon("invalid","true")
  return
}
def timestamp = context.get("timestamp")
password = context.get("_.password")
pw = context.getSpecial(timestamp+"|"+rolonUuid+".password")
if (pw != null && !pw.validate(password)) {
  context.setCon("invalid","true")
  return
}
if (pw == null && (rolonUuid != context.ADMIN_USER_UUID || password != "password")) {
  context.setCon("invalid","true")
  return
}
def extendedContext = context.getSpecial(".extendedContext")
def updateParameters = extendedContext.updateParameters
def oldRequest = context.get("_.oldRequest")
if (oldRequest == "") oldRequest = "/templates/En/organizer/"   //TODO: don't hard code this
def userLanguage = context.get(timestamp+"|"+rolonUuid+".att.userLanguage")
def timezone = context.get(timestamp+"|"+rolonUuid+".att.timezone")
updateParameters.put("newUser.rolonUuid",rolonUuid)
context.setCon("user.uuid", rolonUuid)
updateParameters.put("templateRequest",oldRequest)
oldUuid = context.get("_.rolonUuid")
updateParameters.put("_.rolonUuid",oldUuid)
past = context.get("_.past")
updateParameters.put("_.past",past)
updateParameters.put("_.timestamp",past)
if (userLanguage != "")
  updateParameters.put("user.language",userLanguage)
if (timezone != "")
  updateParameters.put("user.timezone",timezone)
extendedContext.createJournalEntry(context, context.LOGON_NAME)
extendedContext.addJournalEntryRelationships(rolonUuid, context.LOGON_UUID)
extendedContext.addJournalEntryRelationship(context.ACCESS_RELATIONSHIP, rolonUuid, "reader")
