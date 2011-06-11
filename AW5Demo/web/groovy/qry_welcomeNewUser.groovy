if (context.get("newUser.duplicate").length() > 0) {
  context.setCon("greeting","That name is taken")
} else {
  context.setCon("greeting","Welcome ${context.get("newUser.name")} to AgileWiki5")
}
