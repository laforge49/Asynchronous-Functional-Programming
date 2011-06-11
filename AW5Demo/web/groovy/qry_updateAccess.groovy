access = context.get("_.access")
if (access != "") {
  if (access == "noChange") context.setCon("noChange","true")
  else if (access == "none") context.setCon("none","true")
  else if (access == "reader") context.setCon("reader","true")
  else if (access == "writer") context.setCon("writer","true")
  else if (access == "owner") context.setCon("owner","true")
}
pAccess = context.get("pAccess")
if (pAccess != "") {
  if (pAccess == "none") context.setCon("pNone","true")
  else if (pAccess == "reader") context.setCon("pReader","true")
  else if (pAccess == "writer") context.setCon("pWriter","true")
  else if (pAccess == "owner") context.setCon("pOwner","true")
}
