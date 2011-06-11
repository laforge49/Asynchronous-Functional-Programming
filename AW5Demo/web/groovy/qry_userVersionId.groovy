def timestamp = context.get("user.timestamp")
def uuid = context.get("user.uuid")
def id = timestamp + "|" + uuid
context.setCon("user.versionId",id)
