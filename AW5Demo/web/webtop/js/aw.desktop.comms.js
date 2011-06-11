/*
This file is part of AgileWiki and is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License (LGPLv3) as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.

This code is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General 
Public License v3 for more details: http://www.opensource.org/licenses/lgpl-3.0.html

Note however that only Java and JavaScript files are being covered by LGPL, 
all other files are covered by the Common Public License (CPL). A copy of 
this license can be found as well at http://www.opensource.org/licenses/cpl1.0.txt
*/


// Namespaces for the aw.desktop implementation
this.aws = this.aws || {};
aws.desktop = {};

aws.desktop.comms = function(rtl)
{
	var channelId = $("#channelId").val();
	
	var awSubscription = null;
	var metaSubscriptions = [];
	var commsConnected = false;
	
	_cometd = $.cometd;
	
	var selectedWindow = null;
	
	// public functions
	
	this.init = function init()
	{
		_cometd.init('/comet');
		awSubscribe();
		metaSubscribe();		
	}
	
	this.publish = function publish(msg)
	{
		$.debug("publishing ...");
		_cometd.publish(channelId, msg);
	}
	
	// private functions
	
	function awUnsubscribe()
	{
		if (awSubscription) _cometd.unsubscribe(awSubscription);
      awSubscription = null;
	}	
	
	function awSubscribe()
	{
		awUnsubscribe();
		awSubscription = _cometd.subscribe(channelId, cometCallBack);
	}
	
	
   function metaUnsubscribe()
   {
       $.each(metaSubscriptions, function(index, subscription)
       {
           _cometd.removeListener(subscription);
       });
       metaSubscriptions = [];
   }

   function metaSubscribe()
   {
       metaUnsubscribe();
       //_metaSubscriptions.push(_cometd.addListener('/meta/handshake', this, _metaHandshake));
       metaSubscriptions.push(_cometd.addListener('/meta/connect', this, metaConnect));
   }
	
	function metaConnect(message)
	{
		var wasConnected = commsConnected;
		commsConnected = message.successful;
		if (wasConnected)
		{
			if (commsConnected)
			{
				// Normal operation, a long poll that reconnects
				$('#connectStatus').removeClass().addClass('connectGreen');
				$.debug('CONNECT STATUS GREEN');
			}
			else
			{
				// Disconnected
				// We can feedback to the user here
				$('#connectStatus').removeClass().addClass('connectAmber');
				$.debug('CONNECT STATUS AMBER');
			}
		}
		else
		{
			if (commsConnected)
			{
				// Reconnected
                
				$.debug("RECONNECTED");
                
				var resyncMsg = "{'target': '~/Desktop', 'userEvent': {'action': 'Resync','wrappers': {";
            
            var itemsToSync = 0;
               
				$( "#desktop-icons .icon" ).each(function() 
				{
					var icon = $(this);

					resyncMsg += "'"+icon.data('name')+"': {";
					resyncMsg += "'timestamp': '"+icon.data('timestamp')+"',";
					resyncMsg += "'id': '"+icon.attr('id')+"',";
					resyncMsg += "'xPosition': "+icon.data('xPosition')+",";
					resyncMsg += "'yPosition': "+icon.data('yPosition')+",";
					resyncMsg += "'selected': "+icon.data('selected')+"";
					resyncMsg += "},";
					itemsToSync++;
				});
					
				$( "#windows .window" ).each(function() 
				{
					var win = $(this);

					resyncMsg += "'"+win.data('name')+"': {";
					resyncMsg += "'timestamp': '"+win.data('timestamp')+"',";
					resyncMsg += "'id': '"+win.attr('id')+"',";
					resyncMsg += "'xPosition': "+win.data('xPosition')+",";
					resyncMsg += "'yPosition': "+win.data('yPosition')+",";
					resyncMsg += "'selected': "+win.data('selected')+",";
					resyncMsg += "'xSize': "+win.data('xSize')+",";
					resyncMsg += "'ySize': "+win.data('ySize')+",";
					resyncMsg += "'mode': '"+win.data('mode')+"',";
					resyncMsg += "'view': '"+win.data('view')+"'";
					resyncMsg += "},";
					itemsToSync++;
				});
					
				// strip off trailing comma
				resyncMsg = resyncMsg.substring(0,resyncMsg.length-1);
				resyncMsg += "}}}";

				
				if (itemsToSync > 0)
				{
					_cometd.startBatch();
					awSubscribe();  
					$.comms.publish(eval('('+resyncMsg+')'));
					_cometd.endBatch();
				}
			}
			else
			{
				// Could not connect
				// Feedback to the user
				$('#connectStatus').removeClass().addClass('connectRed');
				$.debug('CONNECT STATUS RED');
			}
		}
	}
	
	// Temporary test to see if resync message can work under the right conditions
	
	/*$('#connectStatus').click(function() 
	{
			var resyncMsg = "{'target': '~/Desktop', 'userEvent': {'action': 'Resync','wrappers': {";
            
            var itemsToSync = 0;
               
				$( "#desktop-icons .icon" ).each(function() 
				{
					var icon = $(this);

					resyncMsg += "'"+icon.data('name')+"': {";
					resyncMsg += "'timestamp': '"+icon.data('timestamp')+"',";
					resyncMsg += "'id': '"+icon.attr('id')+"',";
					resyncMsg += "'xPosition': "+icon.data('xPosition')+",";
					resyncMsg += "'yPosition': "+icon.data('yPosition')+",";
					resyncMsg += "'selected': "+icon.data('selected')+"";
					resyncMsg += "},";
					itemsToSync++;
				});
					
				$( "#windows .window" ).each(function() 
				{
					var win = $(this);

					resyncMsg += "'"+win.data('name')+"': {";
					resyncMsg += "'timestamp': '"+win.data('timestamp')+"',";
					resyncMsg += "'id': '"+win.attr('id')+"',";
					resyncMsg += "'xPosition': "+win.data('xPosition')+",";
					resyncMsg += "'yPosition': "+win.data('yPosition')+",";
					resyncMsg += "'selected': "+win.data('selected')+",";
					resyncMsg += "'xSize': "+win.data('xSize')+",";
					resyncMsg += "'ySize': "+win.data('ySize')+",";
					resyncMsg += "'mode': '"+win.data('mode')+"',";
					resyncMsg += "'view': '"+win.data('view')+"'";
					resyncMsg += "},";
					itemsToSync++;
				});
					
				// strip of trailing comma
				resyncMsg = resyncMsg.substring(0,resyncMsg.length-1);
				resyncMsg += "}}}";

				
				if (itemsToSync > 0)
				{
					_cometd.startBatch();
					//awSubscribe();  
					$.comms.publish(eval('('+resyncMsg+')'));
					_cometd.endBatch();
				}
				
			return(false);  
	});*/
	
	
	// setup desktop icons
	function setupIcon(data) 
	{
		var self = $("#desktop-icons #"+data.id);
		var selfTxt = $("#desktop-icons #txt"+data.id )
		
		self.data('timestamp',data.timestamp);
		self.data('name',data.name);
		self.data('xPosition',data.xPosition);
		self.data('yPosition',data.yPosition);
		self.data('selected',data.selected);
		
		self.draggable({
			stop: function(event, ui) { 

				var pos = $(this).position();

				$.comms.publish({'target': '~/Desktop/'+data.name, 'userEvent': {'action': 'MoveIcon', 'xPosition':$.mirrorIfRtl(rtl,Math.round(pos.left)),'yPosition': Math.round(pos.top)}});
			},
			start: function(event, ui) { 
				$(this).trigger('click');
			}
		});

		// if icon is selected highlight
		if (data.selected)
		{
			$.debug("----------> Icon Selected.");
			self.addClass("icon-focus");
		}

		// add graphic and position icon
		

		
		self.css("background-image", "url('"+data.image+"')").css("left",$.mirrorIfRtl(rtl,data.xPosition)+"px").css("top",data.yPosition+"px").css("position","absolute");
		
		
		//Cannot use this technique as drag doesn't work if left isn't defined
		//$("#desktop-icons #"+data.id).css("background-image", "url('"+data.image+"')").fadeTo("fast", 0.7).css(xOrigin,data.xPosition+"px").css("top",data.yPosition+"px").css("position","absolute");
		

		self.click(function() 
		{
			$.comms.publish({'target': '~/Desktop/'+data.name, 'userEvent': {'action': 'Select'}});
			$(".icon").removeClass("icon-focus");
			$(this).addClass("icon-focus");
			//$(this).fadeTo("fast", 1.0);  
			$("#icon-menu").fadeOut("fast");
			
			return(false);  
			// Returning false here to prevent the bubbling up of events, 
			// otherwise a click on an icon would also trigger a click on the desktop.
		});
		
		self.dblclick(function(e) 
		{
			// checks to see if the textbox has been clicked on - doesn't fire OpenIcon if it hasn't
			// maybe a neater way of doing this
			if (e.originalTarget == e.currentTarget)
			{	
				$.comms.publish({'target': '~/Desktop/'+data.name, 'userEvent': {'action': 'OpenIcon'}});
			}
			return(false);  
			// Returning false here to prevent the bubbling up of events, 
			// otherwise a click on an icon would also trigger a click on the desktop.
		});
		
		self.rightClick( function(e) 
		{		
			_cometd.publish(channelId, {'target': '~/Desktop/'+data.name, 'userEvent': {'action': 'Select'}});
			//$.comms.publish({'target': '~/Desktop/'+data.name, 'userEvent': {'action': 'Select'}});
			var self = $(this);
			$(".icon").removeClass("icon-focus");
			self.addClass("icon-focus");
			self.fadeTo("fast", 1.0);  



			//$("#icon-menu").show().fadeTo("fast", 0.7).css("left",e.pageX).css("top",e.pageY).css("position","absolute");
			
			//$("#icon-menu").fadeIn().css("left",e.pageX).css("top",e.pageY).css("position","relative");
			var scrollLeft = $('#desktop-holder').scrollLeft();
			var scrollTop = $('#desktop-holder').scrollTop();
			
			/*$("#icon-menu").queue(function()
			{
				$(this).fadeIn().css("left",e.pageX+scrollLeft).css("top",e.pageY+scrollTop).css("position","absolute").dequeue();
			});*/
			
 			$("#icon-menu").fadeIn().css("left",e.pageX+scrollLeft).css("top",e.pageY+scrollTop).css("position","absolute");
 
 
 			// Delete Icon
 			$("#icon-menu #close-icon").click( function(e)
 			{
				
				//_cometd.publish(channelId, {'target': '~/Desktop/'+data.name, 'userEvent': {'action': 'DeleteIcon'}});
				$.comms.publish({'target': '~/Desktop/'+data.name, 'userEvent': {'action': 'DeleteIcon'}});

				
				//should we do this here or wait for the message for consistency ?
				
				self.remove();

				$("#icon-menu").fadeOut("fast");
				e.preventDefault();
				return(false);
			});
			
			// Rename icon
 			$("#icon-menu #rename-icon").click( function(e) 
 			{
				renameIcon(selfTxt);
				$("#icon-menu").fadeOut("fast");
				e.preventDefault();
				return(false);
			});
			
 			$("#icon-menu #open-window").click( function(e) 
 			{
				//_cometd.publish(channelId, {'target': '~/Desktop/'+data.name, 'userEvent': {'action': 'OpenIcon'}});

				
				$.comms.publish({'target': '~/Desktop/'+data.name, 'userEvent': {'action': 'OpenIcon'}});
				$("#icon-menu").fadeOut("fast");
				e.preventDefault();  
				return(false);
			});
			
			e.preventDefault();
			//e.stopImmediatePropagation();
			return(false);
		});
		
		
		// Prevents browser right-click default behaviour
		$("#icon-menu").noContext();
		$("#kicker").noContext();
		

      // Can we select on class and move these events out of this function?
      // live is not detecting for some reason.
        
		self.hover(function() {
			$(this).addClass("icon-hover");
			//$(this).fadeTo("fast", 1.0);        
		},
		function() {
			$(this).removeClass("icon-hover");
			//$(this).fadeTo("fast", 0.7);
		});
		
		
		function renameIcon(icon)
		{
			if (icon.parent().hasClass("icon-focus") && icon.hasClass("icon-text"))
			{
				icon.removeClass("icon-text");
				icon.addClass("icon-text-edit");
				var thisText = icon.text();
				icon.html('<input class="rename-text" type=text value="'+thisText+'" name="text" size="'+thisText.length+'" />');
				$('.rename-text').focus().select();

				$('.rename-text').bind('dblClick',function()
				{						
					$(this).focus().select();
					return false;
				});
				
				// add the original text to the element as data, needed later for rename icon message

				icon.data("originalName",thisText);
			}		
		}
		
		// trigger rename icon functionality
		selfTxt.click(function()
		{
			renameIcon($(this));
		});

	}
	
	// Callback 
	
	
	function cometCallBack(msg)
	{
      $.debug('>>> msg.data.message ='+msg.data.message);
      
      if (msg.data.message == "Ready")
      {
      	$.debug('--->selectedWindow = '+selectedWindow);
      	if (selectedWindow != null)
      	{
      		$.setWindowFront($(selectedWindow).parent());
      	}

      } 

		// --------------------
		// Icon based messaging
		// --------------------
		
		var icon = $("#desktop-icons #"+msg.data.id);
		var iconTxt = $("#desktop-icons #txt"+msg.data.id)

		if (msg.data.message == "LoadIcon")
		{   
			$("#desktop-icons").append("<div class='icon' id='"+msg.data.id+"' title='"+msg.data.tagline+"'><span id='txt"+msg.data.id+"' class='icon-text'>"+msg.data.name+"</span></div>");
			//$("#windows").append("<div id='wincontent-"+msg.data.id+"' title='"+msg.data.tagline+"'></div>"); 
			setupIcon(msg.data);
		}
		
		if (msg.data.message == "DropIcon")
		{   
			icon.fadeOut('fast').remove();
		}

		if (msg.data.message == "UpdateIcon")
		{   
			// Set the icon image in case it has changed

			if (msg.data.image != null)
			{
				icon.css("background-image","url("+msg.data.image+")");
			}

			if (msg.data.tagline != null)
			{
				$.debug("updating tagline to "+msg.data.tagline,2);				
				$("#"+msg.data.id).attr('title',msg.data.tagline); 
			}
					

			icon.data('timestamp',msg.data.timestamp);
			if (msg.data.selected)
			{
				$(".icon").removeClass("icon-focus");
				icon.addClass("icon-focus");

				// remove focus from window
				$(selectedWindow).prev().removeClass("window-focus");
				$(selectedWindow+"-task").removeClass("task-on");
				
				selectedWindow = null;
				$("#windows").data('selected',selectedWindow);
				
				if (msg.data.xPosition != null && msg.data.yPosition != null)
				{
					icon.css("position","absolute").animate({left:$.mirrorIfRtl(rtl,msg.data.xPosition)+"px",top:msg.data.yPosition+"px",queue:false},1000,"easeOutElastic",function(){
						//console.log("select scroll after pos change");
						$.scrollToIcon(msg.data.id);
					});
				}
				else
				{
					$.scrollToIcon(msg.data.id);
				}
			}
			else
			{
				icon.removeClass("icon-focus");

				if (msg.data.xPosition != null && msg.data.yPosition != null){
					$.scrollToIcon(msg.data.id);
				}
			}	
			
			// Rename icon if name is different
			
			if (msg.data.name != iconTxt.text())
			{
				iconTxt.text(msg.data.name);
				icon.data('name',msg.data.name);
			}
			
			icon.data('selected',msg.data.selected);
			icon.data('timestamp',msg.data.timestamp);
			icon.data('xPosition',msg.data.xPosition);
			icon.data('yPosition',msg.data.yPosition);
		}
		
		// --------------------
		// Window based messaging
		// --------------------
		
		$.debug('====================================');
		$.debug('msg.data.message ='+msg.data.message);
		$.debug('msg.data.id ='+msg.data.id);
		$.debug('msg.data.mode ='+msg.data.mode);
		$.debug('msg.data.name ='+msg.data.name);
		$.debug('msg.data.timestamp ='+msg.data.timestamp);		
		$.debug('msg.data.xPosition = '+msg.data.xPosition); 
		$.debug('msg.data.yPosition = '+msg.data.yPosition);
		$.debug('msg.data.height = '+msg.data.height); 
		$.debug('msg.data.width = '+msg.data.width);
		//$.debug('msg.data.content = '+msg.data.content);  
		$.debug('====================================');
		
		
		
		// CHANGE THIS!!!
		var self = "#"+msg.data.id;
		
		if (msg.data.message == "LoadWindow")
		{   
			$("#windows").append("<div id='"+msg.data.id+"' class='window' title='"+msg.data.tagline+"'></div>");
			
			// evals replaced with parseInts below
			
			//$(self).window({ url: "../window.jsp?", width: parseInt(msg.data.width), height: parseInt(msg.data.height), top: parseInt(msg.data.yPosition),  left: parseInt($.mirrorIfRtl(rtl,msg.data.xPosition,msg.data.width)), modal: false, icon: msg.data.image, rtl: rtl});

			// -------------------------------------- New iFrame stuff! ---------------------------------------- //	

			// Create iFrame


			function createIframe (iframeName, width, height, iframeSrc) {
				var iframe;
				if (document.createElement && (iframe = document.createElement('iframe'))) 
				{
					iframe.name = iframe.id = iframeName;
					iframe.width = '100%';
					iframe.height = '98%';
					//iframe.scrolling = 'no';
					iframe.frameborder = '0';
					if (iframeSrc != null)
					{
						iframe.src = iframeSrc;
					}
					document.body.appendChild(iframe);


				}
				return iframe;
			}

			//var iframe = createIframe ('iframe'+msg.data.id, msg.data.width, msg.data.height,  'window.htm');
			var iframe = createIframe ('iframe'+msg.data.id, msg.data.width, msg.data.height,  msg.data.content);
			/*if (iframe) {
		
				if (iframe.contentDocument) {
					iframeDoc = iframe.contentDocument;
				}
				else if (iframe.contentWindow) {
					iframeDoc = iframe.contentWindow.document;
				}
				else if (window.frames[iframe.name]) {
					iframeDoc = window.frames[iframe.name].document;
				}
				if (iframeDoc) { 

					// Important to open and close the document  
					// http://stackoverflow.com/questions/997986/write-elements-into-a-child-iframe-using-javascript-or-jquery
			
					iframeDoc.open();			

					// Insert message content into iframe

					$.debug("trying to load content into iFrame ......................................................",1);

					//$("html", iframeDoc).appendTo('moooooooooo');
					//$("html", iframeDoc).load('window.htm');
					iframeDoc.write('<html><body><a href="#" id="test">Kabology for all.<\/a><\/body><\/html>');
					
					iframeDoc.close();
				}
			}*/

			//document.body.appendChild(iframe);
			$.debug("iFrame contents = "+iframe,1);

			//console.dir(iframe);

			//$.debug("iFrame contents = "+iframe.document,1);

			$(self).window({ content: iframe, width: parseInt(msg.data.width), height: parseInt(msg.data.height), top: parseInt(msg.data.yPosition),  left: parseInt($.mirrorIfRtl(rtl,msg.data.xPosition,msg.data.width)), modal: false, icon: msg.data.image, rtl: rtl, selectedWindow : selectedWindow});


  			$('iframe#iframe'+msg.data.id).load(function() 
    		{
        		//alert('this');

				/*$('#test',frames['iframe'+msg.data.id].document).bind('click', function() 
				{			
					// Can do a test here to establish context and if appropriate take the href of the link and open it with target '_top'    
					alert('mooooooooo');
					return false;
				});*/

				$('.launchLink',frames['iframe'+msg.data.id].document).bind('click', function() 
				{			
					// Can do a test here to establish context and if appropriate take the href of the link and open it with target '_top'    
					var url = $(this).attr('href');					
					var uuid = $.getUrlParam('rolonUuid',url);
					var timestamp = $.getUrlParam('timestamp',url);
					var xPosition =  $(self).data('xPosition');
					var yPosition =  $(self).data('yPosition');
					$.comms.publish({'target': '~/Desktop/', 'userEvent': {'action': 'NewWindow', 'uuid': uuid, 'xPosition': xPosition, 'yPosition': yPosition, 'timestamp': timestamp}});
					return false;
				});

    		});

			//$('#iframe0').contents('muahahahahha');

			// http://stackoverflow.com/questions/997986/write-elements-into-a-child-iframe-using-javascript-or-jquery

			var frmObj = $('#iframe'+msg.data.id);

			frmObj.attr('frameborder','0');

			/*function windowAdded()
			{
				alert('window added');
			}*/

			
			//alert('added');

			// =================================
			// uncomment this for html injection 

			/*frmNode = frmObj.get(0);
			frmNode = (frmNode.contentWindow) ? frmNode.contentWindow : (frmNode.contentDocument.document) ? frmNode.contentDocument.document : frmNode.contentDocument;
			frmNode.document.open();
			frmNode.document.write(msg.data.content);
			frmNode.document.close();*/

			// end uncomment this for html injection
			// ================================== 

			// set the stylesheet

			//frmObj.find('head').append('<link rel="stylesheet" href="style2.css" type="text/css" />');
			$('head',frames['iframe'+msg.data.id].document).append('<link type="text/css" rel="stylesheet" href="system/css/windowcontent.css"/>');


			

			// -------------------------------------- New iFrame stuff! ---------------------------------------- //	
			
			
			// old working version
			// $(self).window({ content: msg.data.content, width: parseInt(msg.data.width), height: parseInt(msg.data.height), top: parseInt(msg.data.yPosition),  left: parseInt($.mirrorIfRtl(rtl,msg.data.xPosition,msg.data.width)), modal: false, icon: msg.data.image, rtl: rtl, selectedWindow : selectedWindow});


			
			$.debug("(loading) raw xPosition = "+msg.data.xPosition,5);
			$.debug("(loading) raw width = "+msg.data.width,5);
			$.debug("loading xPosition = "+$.mirrorIfRtl(rtl,msg.data.xPosition,msg.data.width),5);
			
			//$(self).window({ content: msg.data.content, width: parseInt(msg.data.width), height: parseInt(msg.data.height), top: parseInt(msg.data.yPosition),  left: parseInt($.mirrorIfRtl(rtl,msg.data.xPosition,0)), modal: false, icon: msg.data.image, rtl: rtl, selectedWindow : selectedWindow});

			// Set TitleBar
			$(self).prev().attr("id","titleBar"+msg.data.id);
 
			$.debug('LoadWindow received.');
			$.debug('msg.data.mode ='+msg.data.mode);
			$.debug('msg.data.name ='+msg.data.name);
			$.debug('msg.data.timestamp ='+msg.data.timestamp);
			//scrollToWindow(msg.data.id);
			
			// wonder maybe if might not be simpler to add all msg.data to the element via data()
			
			$(self).data('name',msg.data.name);
			$(self).data('timestamp',msg.data.timestamp);
			$(self).data('mode',msg.data.mode);
			$(self).data('view',msg.data.internalView);
			$(self).data('xPosition',msg.data.xPosition);
			$(self).data('yPosition',msg.data.yPosition);
			$(self).data('selected',msg.data.selected);
			$(self).data('xSize',msg.data.width);
			$(self).data('ySize',msg.data.height);
			
			
			if (msg.data.mode == 'maximized')
			{
				$(self).data("trigger", { remote: true });
				$.debug("triggering maximize");
				$(self).dialog("maximize");
				$(self).data("maximized", true );			
			}
			
			if (msg.data.mode == 'minimized')
			{
				$(self).data("trigger", { remote: true });
				$.debug("triggering minimize");
				$(self).dialog("minimize");
			}
			
			if (msg.data.mode == 'normal')
			{
				$(self).data("maximized", false );
				$(self).data("minimized", false );	
			}
			
			$.debug("msg.data.selected ="+msg.data.selected);
			
			if (msg.data.selected)
			{
				$.scrollToWindow(msg.data.id);
				// change state of taskbar buttons
				$(".task").removeClass("task-on");
				$("#"+msg.data.id+"-task").addClass("task-on");
				$(".ui-dialog-titlebar").removeClass("window-focus");
				$(self).prev().addClass("window-focus");
				$.debug('----------------------------------> selectedWindow set as #'+msg.data.id);
				selectedWindow = "#"+msg.data.id;
				$("#windows").data('selected',selectedWindow);
			}
			//checkState(msg.data,true);		
			
			// titlebar css changes for RTL
			// Should all really be done in the CSS and switch classes only in JS
		}
		
		if (msg.data.message == "UpdateWindow")
		{  
			$.debug('UpdateWindow message received',2); 
			$.debug('msg.data.mode = '+msg.data.mode,2);
			$.debug('bring window to the front...'); 
			$.setWindowFront($(self).parent());
			checkState(msg.data); 
			$(self).data('timestamp',msg.data.timestamp);
			$(self).data('mode',msg.data.mode);
			$(self).data('view',msg.data.internalView);
			
			// only store dimensions if they are not null ?
			
			$(self).data('xPosition',msg.data.xPosition);
			$(self).data('yPosition',msg.data.yPosition);
			$(self).data('selected',msg.data.selected);
			$(self).data('xSize',msg.data.width);
			$(self).data('ySize',msg.data.height);
			

			//$(self).html(msg.data.content);

			//alert($(self).html());

			if (msg.data.content != null)
			{
				var iFrame = $(self).find('iFrame');				
				var iFrameSrc = iFrame.attr('src');
				
				if (msg.data.content != iFrameSrc)
				{
					iFrame.attr('src',msg.data.content);
				}
			}
			
			if (msg.data.tagline != null)
			{
				$("#titleBar"+msg.data.id+" span span").text(msg.data.tagline); 
			}

			$("#titleBar"+msg.data.id+" .ui-dialog-titlebar-refresh").hide();
		}
		
		if (msg.data.message == "StaleWindow")
		{  
			$.debug('StaleWindow message received'); 

			$("#titleBar"+msg.data.id+" .ui-dialog-titlebar-refresh").show();
		}
		
		//temp for testing
		//$("#titleBar"+msg.data.id+" .ui-dialog-titlebar-refresh").show();
		
		if (msg.data.message == "DropWindow")
		{
			//$(self).dialog("close");
			$(self).dialog("destroy");	
		}
		
		if (msg.data.message == "RefreshDesktop")
		{   
			window.location.reload();			
		}
		
		if (msg.data.message == "Views")
		{   
			//window.location.reload();
			
			$(self).data("trigger", { remote: true });
			
			var views = msg.data.views;	
			$.debug("views.length = "+views.length);
			for (i=0; i < views.length; i++)
			{
				$.debug(views[i][0]);
				$.debug(views[i][1]);
			}
			$(self).data('views',views);
			$(self).dialog("view");		
		}


		function checkState(data)
		{
			var self = $("#"+data.id);

			if (data.mode == 'minimized')
			{
				if ($(self).data("minimized") == null || $(self).data("minimized") != true)
				{
					$(self).dialog("minimize");
				}
			}
			else
			{
				if (data.selected)
				{
					
					// change state of taskbar buttons
					$(".task").removeClass("task-on");
					selectedWindow = "#"+data.id;
					$("#windows").data('selected',selectedWindow);
					$.debug("comms.js selectedWindow = "+selectedWindow,3);
					$("#"+data.id+"-task").addClass("task-on");
					
					$(".ui-dialog-titlebar").removeClass("window-focus");
					$(self).prev().addClass("window-focus");
					
					$.debug("data.selected = true");
					
					$.debug('msg.data.selected = '+msg.data.selected); 
					$.debug('msg.data.xPosition = '+msg.data.xPosition); 
					$.debug('msg.data.yPosition = '+msg.data.yPosition);
					
					// It is possible to receive a message from a refresh where the mode is maximized and still have height, width and coordinates
    
					if (data.xPosition != null && data.yPosition != null && data.mode != 'maximized')
					{
						
						$.debug("(setting) raw xPosition = "+data.xPosition,5);
						$.debug("(setting) (msg) raw xPosition = "+msg.data.xPosition,5);
						$.debug("(setting) raw width = "+data.width,5);
						$.debug("(setting) (msg) raw width = "+msg.data.width,5);
						$.debug("(setting) $(self).data('xSize') = "+$(self).data('xSize'),5);
						$.debug("setting xPosition = "+$.mirrorIfRtl(rtl,data.xPosition,data.width),5);
						$(self).parent().css("position","absolute").animate({left:$.mirrorIfRtl(rtl,data.xPosition,$(self).data('xSize'))+"px",top:data.yPosition+"px",queue:false},1000,"easeOutElastic",function(){
							$.debug("scrolling to window (positions null)");
							$.scrollToWindow(data.id);
						});
						
						$(self).data('xPosition',data.xPosition);
						$(self).data('yPosition',data.yPosition);
						
					}
					else
					{
						$.debug("No xPosition or yPosition.");
						
						// As for certain commands windows are placed after the scroll command is issued we want to scroll once windows are position
						
						if (data.mode != 'normal' && data.mode != 'maximized')
						{
							$.scrollToWindow(data.id);
						}
					}
				
					// It is possible to receive a message from a refresh where the mode is maximized and still have height, width and coordinates
					
					if (data.width != null && data.height != null && data.mode != 'maximized')
					{
						$.debug("rtl="+rtl);
						//$.debug("new width ="+$.mirrorIfRtl(rtl,data.xPosition));
						
						//$(self).parent().css("position","absolute").animate({width:$.mirrorIfRtl(rtl,data.width)+"px",height:data.height+"px",queue:false},1000,"easeOutElastic",function(){
						//});
						
						$(self).parent().css("position","absolute").animate({width:data.width+"px",height:data.height+"px",queue:false},1000,"easeOutElastic",function(){
						});
						
						$(self).data('xSize',data.width);
						$(self).data('ySize',data.height);
					
						//$("#windows #"+data.id).css("height",data.height+"px");
						
						$.debug("title bar height = "+$('.ui-dialog-titlebar').css('height'));
						
					
						// nudged by 40 until we have a proper window footer
						var height = parseInt(data.height)-parseInt($('.ui-dialog-titlebar').css('height'))-40;
						$("#windows #"+data.id).css("height",height+"px");
					}
				}
				else
				{
					if (data.xPosition != null && data.yPosition != null){
						$.scrollToWindow(data.id);
					}
				}
			}
			  
			// Checking for maximized state
			
			if (data.mode == 'maximized')
			{
				$.debug("#"+data.id+" - msg.data.mode = maximized");
				$.debug("maximized --->"+$(self).data("maximized"),2);
				$.debug("minimized --->"+$(self).data("minimized"),2);
				
				// Need to not trigger the maximise on refresh
				
				if ($(self).data("minimized") == true || (($(self).data("minimized") == false && $(self).data("maximized") == false)))
				{
					$(self).data("trigger", { remote: true });
				
					$.debug("(maximized) triggering maximize",2);
					$(self).dialog("maximize");
					$(self).data("maximized", true );
					$.debug('Setting Maximized = true'); 
				}
			}
			
			// Checking for normal state
			
			if (data.mode == 'normal')
			{
				$.debug("msg.data.mode = normal");

				if ($(self).data("maximized") || $(self).data("minimized"))
				{
					$(self).data("trigger", { remote: true });

					$.debug("(normal) triggering maximize");
					$(self).dialog("maximize");
					$(self).data("maximized", false );
					$.debug('Setting Maximized = false'); 
				}
                  
			}
		}
		//if (msg.data.message == 'Ready') alert('stop');
	}
}


// Alternative method of doing things

/*$.comms = 
{
	channelId : $("#channelId").val(),
	awSubscription : null,
	metaSubscriptions : [],
	commsConnected : false,
				
	_cometd : $.cometd, 
	
	
	init : function()
	{
		
		_cometd.init('/comet');
	},
	
	publish : function(msg)
	{
		alert('test');
		$.comms._cometd.publish(channelId, msg);	
	}
}*/

	
