if (context.get("updateRequestInError") != "") {
  return
}
rolonUuid = context.get("rolonUuid")
def extendedContext = context.getSpecial(".extendedContext")
if (!extendedContext.hasPrivilege(context, rolonUuid, "owner")) return
negative = context.get("_.negative")
hr = context.get("_.hr")
mn = context.get("_.mn")
userUuid = context.get("user.uuid")
if (rolonUuid == userUuid) {
  oldTimezone = context.get("user.timezone")
} else {
  timestamp = context.get("timestamp")
  oldTimezone = context.get(timestamp + "|" + rolonUuid + ".att.timezone")
}
if (hr.length() == 0) {
  if (oldTimezone.startsWith("-")) context.setCon("_.negative","M")
  context.setCon("_.hr",oldTimezone.substring(1,3))
  context.setCon("_.mn",oldTimezone.substring(4,6))
  return
}
if (negative == "M") timezone = "-"
else timezone = "+"
timezone = timezone + hr + ":" + mn
if (timezone == oldTimezone) return
def updateParameters = extendedContext.updateParameters
extendedContext.assignViewParameters(context)
if (rolonUuid == userUuid) {
  updateParameters.put("user.timezone",timezone)
}
extendedContext.createJournalEntry(context, context.TIMEZONE_NAME + ": " + timezone)
extendedContext.modelJournalEntryAccess(context, rolonUuid)
extendedContext.addJournalEntryRelationships(userUuid, context.TIMEZONE_UUID)
extendedContext.addJournalEntryRelationship(context.EFFECTS_RELATIONSHIP, rolonUuid, " ")
extendedContext.addJournalEntryChangeRealmRelationship(context, rolonUuid)
extendedContext.attributeSpec(rolonUuid, "timezone", timezone)
