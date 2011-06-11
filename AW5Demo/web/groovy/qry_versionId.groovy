def timestamp = context.get("timestamp")
def uuid = context.get("uuid")
def id = timestamp + "|" + uuid
context.setCon("versionId",id)
