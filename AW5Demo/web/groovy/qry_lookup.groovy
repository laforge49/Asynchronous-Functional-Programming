def name=context.get("name")
def mapName = context.get("map")
def map = context.getSpecial(mapName)
context.setCon("value",map.get(name))
