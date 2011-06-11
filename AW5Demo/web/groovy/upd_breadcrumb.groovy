if (context.get("visited").length() > 0) return
ts = context.get("_.past")
if (ts.length() > 0) return
userUuid = context.get("user.uuid")
if (userUuid == context.ANONYMOUS_UUID) return
rolonUuid = context.get("_rolonUuid")
breadcrumbs = context.getSpecial("user.breadcrumbs")
if (breadcrumbs.size() > 0) {
  first = breadcrumbs.get(0)
  if (breadcrumbs.contains(rolonUuid)) {
    if (rolonUuid == first) return
  }
}
extendedContext = context.getSpecial(".extendedContext")
def updateParameters = extendedContext.updateParameters
updateParameters.put("visited", "true")
extendedContext.assignViewParameters(context)
extendedContext.createJournalEntry(context, null)
breadcrumbs.remove(rolonUuid)
breadcrumbs.add(0,rolonUuid)
if (breadcrumbs.size() > 10) {
  breadcrumbs.remove(breadcrumbs.size() - 1)
}
updateParameters.put("user.breadcrumbs", breadcrumbs)
