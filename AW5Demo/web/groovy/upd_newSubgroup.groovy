def newGroupName = context.get("_.newGroupName")
if (newGroupName == "") return
def supergroupUuid = context.get("_.supergroup")
if (supergroupUuid == "") return
def extendedContext = context.getSpecial(".extendedContext")
def i = supergroupUuid.indexOf("_")
if (i == -1) return
def supergroupRoleName = supergroupUuid.substring(0,i)
def supergroupRole = extendedContext.role(context.typeName(supergroupRoleName))
if (supergroupRole == null) return
if (!supergroupRole.isA(context.GROUP_TYPE)) return
if (!extendedContext.hasPrivilege(context,supergroupUuid,"writer")) return
def updateParameters = extendedContext.updateParameters
extendedContext.assignViewParameters(context)
def tagLine = context.get("_.tagLine")
updateParameters.put("newGroup.name",newGroupName)
extendedContext.createJournalEntry(context, "new subgroup: " + newGroupName)
extendedContext.addJournalEntryRelationship(context.ACCESS_RELATIONSHIP, context.ACCESS_UUID, "reader")
def newGroupUuid = extendedContext.create(supergroupRoleName)
updateParameters.put("newGroup.rolonUuid", newGroupUuid)
extendedContext.attributeSpec(newGroupUuid, "tagLine", tagLine)
extendedContext.addRelationship(newGroupUuid, context.PARENT_RELATIONSHIP, context.GROUPS_UUID, newGroupName)
extendedContext.addRelationship(newGroupUuid, context.ACCESS_RELATIONSHIP, context.ACCESS_UUID, "reader")
userUuid = context.get("user.uuid")
extendedContext.addRelationship(newGroupUuid, context.ACCESS_RELATIONSHIP, userUuid, "owner")
