membership = context.get("_.membership")
if (membership == "readerMembers") context.setCon("readerMembers","true")
else if (membership == "writerMembers") context.setCon("writerMembers","true")
else if (membership == "ownerMembers") context.setCon("ownerMembers","true")
