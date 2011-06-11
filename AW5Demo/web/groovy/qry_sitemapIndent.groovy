def keyName = context.get("keyName")
def key = context.get(keyName)
def array = key.split("/")
def count = array.length - 4
def classPrefix = context.get("classPrefix")
def cls = classPrefix + count
context.setCon("className",cls)
