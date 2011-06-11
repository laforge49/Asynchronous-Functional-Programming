def loopPrefix = context.get("loopPrefix")
if (loopPrefix.length() > 0)
  loopPrefix = "."+loopPrefix
def timestamp = context.get("timestamp")
def key = context.get(loopPrefix+"key")
if (context.get(timestamp+"|"+key+".role") == "") {
  context.setVar(loopPrefix+"ignore","true")
  return
}
def extendedContext = context.getSpecial(".extendedContext")
def qualificationAttribute = context.get(loopPrefix+"qualificationAttribute")
def qualifiers = context.getSpecial(timestamp+"|"+key+".qualifiers")
def qualifierUuid = context.get(loopPrefix+"qualifierUuid")
def attributes = qualifiers.get(qualifierUuid)
def qualificationAttributeValue = attributes.get(qualificationAttribute)
if (qualificationAttributeValue == null) context.setVar(loopPrefix+"ignore","true")
else context.setVar(loopPrefix+"extension",qualificationAttributeValue)
