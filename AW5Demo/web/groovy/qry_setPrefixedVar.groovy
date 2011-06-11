def name = context.get("name")
def prefix = context.get("prefix")
if (prefix != "") name = prefix + "." + name
def value = context.get("value")
context.setVar(name,value)
