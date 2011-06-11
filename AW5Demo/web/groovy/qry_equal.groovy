def a = context.get("a")
def b = context.get("b")
if (a == b) {
  context.setCon("equal", "true")
} else {
  context.setCon("equal", "")
}
