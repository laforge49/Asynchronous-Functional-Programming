import org.agilewiki.web.comet.CometChannel
def userUuid = context.get("user.uuid")
if(userUuid.trim().length() != 0) {
  context.setCon("user.channelId", CometChannel.userChannel(userUuid))
}
