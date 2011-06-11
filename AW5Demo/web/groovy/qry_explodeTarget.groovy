def target = context.get("comet.data.target").split("/")
if (target.size() > 0) {
  context.setCon("target.user", target[0])
  if (target.size() > 1) {
    context.setCon("target.desktop", target[1])
    if (target.size() > 2) {
      context.setCon("target.wrapper", target[2])
    }
  }
}