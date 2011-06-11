breadcrumbs = context.getSpecial("user.breadcrumbs")
ns = new java.util.TreeSet()
s = breadcrumbs.size
i = 0
while(i < s) {
  uuid = breadcrumbs.get(i)
  k = "" + (i + 100) + (char) 5 + uuid
  ns.add(k)
  i = i + 1
}
extendedContext = context.getSpecial(".extendedContext")
sequence = extendedContext.navigableSequence(ns,false)
contextFilterSequence = extendedContext.contextFilterSequence(context,sequence,"subjectKeys")
loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = loopPrefix+"."
context.setSpecial(loopPrefix+"sequence",contextFilterSequence)
context.setCon(loopPrefix+"isUuidSequence","true")
