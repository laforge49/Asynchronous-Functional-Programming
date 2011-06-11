<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.agilewiki.servlets.helpers.*" %>
<%@ page import="org.agilewiki.webtop.DesktopHelper" %>
<%@ page import="org.agilewiki.webtop.DesktopLabels" %>
<%@ page import="org.agilewiki.comet.CometChannelUtility" %>
<%
	String principal = request.getUserPrincipal().getName();
%>
<%
	DesktopHelper desktopHelper = new DesktopHelper(principal);
%> 
<%
 	int desktopSizeX = desktopHelper.getDesktopSizeX();
 %>
<%
	int desktopSizeY = desktopHelper.getDesktopSizeY();
%>
<%
	String channelId = CometChannelUtility.getChannelId(principal);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<% String dir; %>
<% if (desktopHelper.isRightToLeft()) { %>
  <% dir = "rtl"; %>
<% } else { %>
  <% dir = "ltr"; %>
<% } %>
<!--<html dir="<%=dir%>">-->
<html dir="<%=dir%>">
	<head>
	<meta http-equiv="Content-type" content="text/html; charset=UTF-8" />
	<title>AgileWiki Desktop</title>

		<link rel="stylesheet" href="system/css/default.css" type="text/css" />
		<link rel="stylesheet" href="system/desktop/setup.icons.css" type="text/css" />
		<link rel="stylesheet" href="system/themes/ui-lightness/jquery-ui-aw.css" type="text/css" media="all" />
		<link rel="stylesheet" href="system/desktop/taskbar/taskbar.css" type="text/css" media="all" />
		<!--<link rel="stylesheet" href="system/css/jquery-ui.aw.css" type="text/css" media="all" />-->
		<!--<link href="apps/nos/jqueryFileTree.css" rel="stylesheet" type="text/css" media="screen" />-->
	
		<!--<script type="text/javascript" src="system/core/jquery-1.3.2.js"></script>-->
		<!--<script type="text/javascript" src="system/core/jquery-1.4.1.min.js"></script>-->
		<script type="text/javascript" src="system/core/jquery-1.4.2.min.js"></script>
		
		
		<!--<script type="text/javascript" src="system/core/jquery-1.3.2.js"></script>-->
    		<script type="text/javascript" src="system/comet/cometd.js"></script>
    		<!--<script type="text/javascript" src="system/comet/AckExtension.js"></script>-->
    		<script type="text/javascript" src="system/core/jquery.json-1.3.js"></script>
    		<script type="text/javascript" src="system/comet/jquery.cometd.js"></script>



		<script type="text/javascript" src="system/ui/core/jquery-ui-1.7.2.custom.js"></script>
		<script type="text/javascript" src="system/ui/ui.dialog.aw.js"></script>
		<!--<script type="text/javascript" src="system/ui/jquery-ui-1.7.2.custom.aw.min.js"></script>-->
		
		<!--<script type="text/javascript" src="system/ui/dev/ui.core.js"></script>
		<script type="text/javascript" src="system/ui/dev/ui.draggable.js"></script>
		<script type="text/javascript" src="system/ui/dev/ui.droppable.js"></script>
		<script type="text/javascript" src="system/ui/dev/ui.resizable.js"></script>
		<script type="text/javascript" src="system/ui/dev/ui.dialog.js"></script>
		<script type="text/javascript" src="system/ui/dev/ui.selectable.js"></script>
		<script type="text/javascript" src="system/ui/dev/ui.slider.js"></script>
		<script type="text/javascript" src="system/ui/dev/ui.dialog.js"></script>-->

		
		<!-- the following files will be merged and minified for deployment -->
		
		<script type="text/javascript" src="system/desktop/aw.desktop.utility.js"></script>
		<script type="text/javascript" src="system/desktop/aw.desktop.window.js"></script>
		<script type="text/javascript" src="system/desktop/aw.desktop.comms.js"></script>
		<script type="text/javascript" src="system/desktop/aw.desktop.main.js"></script>
		
		<!--------------------------------------------------------------------->
		
		
		<script src="apps/nos/jqueryFileTree.js" type="text/javascript"></script>
		
		<script src="system/core/jquery.scrollTo-min.js" type="text/javascript"></script>
		<script src="system/core/jquery.rightClick.js" type="text/javascript"></script>
		
		<!-- uncomment below for IE debugging -->
		<!--<script language="javascript" type="text/javascript" src="http://getfirebug.com/releases/lite/1.2/firebug-lite-compressed.js"></script>-->


	</head>
	<body>
    



	<div id="loader">Loading..</div>

	<div dir = "ltr" id="desktop-holder">
   	<div dir="<%=dir%>" id="desktop-space">
			<div id="desktop-icons"></div>
			<div id="desktop-menus">
			<div id="icon-menu">
        		<a href="#" id="close-icon"><%=desktopHelper.localizeApplication(DesktopLabels.CLOSE_ICON)%></a>
        		<a href="#" id="rename-icon"><%=desktopHelper.localizeApplication(DesktopLabels.RENAME_ICON)%></a>
        		<a href="#" id="open-window"><%=desktopHelper.localizeApplication(DesktopLabels.OPEN_WINDOW)%></a>
			</div>
			</div>
  		<div id="windows">
			<div id="language-settings" title="Language Select" ></div>
			<div id="window-menu"></div>
		</div>

		</div>
	</div>


	<div id="taskbar">
		<div id="kicker"></div>
		<div id="connectStatus" class="connectGreen"></div>
	</div>
  
	<div id="startmenu" class="<%=dir%>"> </div>
	<div id="attribution" class="<%=dir%>"> 
This program was developed by the <a href='http://agilewiki.ning.com/' target="_blank">AgileWiki Community</a>, copyright 2009 by the <a href='http://agilewiki.ning.com/profiles/members/' target="_blank">contributing members</a>; you can redistribute it and/or modify it under the terms of the <a href='http://www.opensource.org/licenses/lgpl-3.0.html' target="_blank">GNU Lesser General Public License (LGPLv3)</a>. Portions of this program are based on the Oxygen Desktop which has been made available under this license by its author, Colceriu Cristian.	
	</div>

	<div id="widgets"></div>
	<form id="parameters">
		<input type="hidden" id="channelId" value="<%=channelId%>" />
		<input type="hidden" id="desktopSizeX" value="<%=desktopSizeX%>" />
		<input type="hidden" id="desktopSizeY" value="<%=desktopSizeY%>" />
		<input type="hidden" id="logoutLabel" value="<%=desktopHelper.localizeApplication(DesktopLabels.LOGOUT)%>" />
		<input type="hidden" id="logoutUrl" value="/logout.jsp" />
		<input type="hidden" id="restoreDefaultDesktopLabel" value="<%=desktopHelper.localizeApplication(DesktopLabels.RESTORE_DEFAULT_DESKTOP)%>" />

	</form>
	</body>
</html>
