limit = context.get("_.limit")
if (limit != "") {
  if (limit == "noChange") context.setCon("noChange","true")
  else if (limit == "none") context.setCon("none","true")
  else if (limit == "reader") context.setCon("reader","true")
  else if (limit == "writer") context.setCon("writer","true")
  else if (limit == "owner") context.setCon("owner","true")
}
pLimit = context.get("pLimit")
if (pLimit != "") {
  if (pLimit == "none") context.setCon("pNone","true")
  else if (pLimit == "reader") context.setCon("pReader","true")
  else if (pLimit == "writer") context.setCon("pWriter","true")
  else if (pLimit == "owner") context.setCon("pOwner","true")
}
