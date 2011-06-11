/*
Copyright 2008, 2009 by Colceriu Cristian

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

/*jQuery.fn.extend
({
    removeCss: function(cssName) {
        return this.each(function() {
            var curDom = $(this);
            jQuery.grep(cssName.split(","),
                    function(cssToBeRemoved) {
                        curDom.css(cssToBeRemoved, '');
                    });
            return curDom;
        });
    }
});*/





$(document).ready(function() {

	
	//$.getScript("system/desktop/debug.js");
	//comms();
	
	
	
	// Precache images
	
	// Precaching 'connection status' images so that they can be displayed even when the connection is lost
	var imgstopreload = ["/images/connect_green.png","/images/connect_amber.png","/images/connect_red.png"];
	for (x in imgstopreload){ (new Image).src = imgstopreload[x]; }            

	var rtl = false;
	var xOrigin = "left";

	
	if ($("html").attr("dir") == "rtl") rtl = true;
	
	$.comms = new aws.desktop.comms(rtl);
	$.comms.init();
	$.comms.publish({'target': '~/', 'userEvent': {'action': 'OpenDesktop'}});

	//$.getScript("system/desktop/window.js");
	//$.getScript("system/desktop/comms.js");
	
	//var _cometd = $.comms();
	
	    
    
	// get theme, background-color and wallpaper
    
	jQuery.fn.initDesktop = function(options) {  
		$.ajax({
			type: "GET",
			url: "system/settings.xml",
			dataType: "xml",
			success: function(xml) {
			
				        
				// Although a nice idea - this isn't working with internet explorer
				// hard coded theme css into index.jsp for now
				        
				//theme = $(xml).find("theme").text();
				//var url = "system/themes/" + theme + "/" + theme + ".all.css";
				/*var url = "system/themes/" + theme + "/jquery-ui.css";
				jQuery(document.createElement('link') ).attr({
					href: url,
					media: 'screen',
					type: 'text/css',
					title: 'theme',
					rel: 'stylesheet'
				}).appendTo('head');*/      
          
				//$("body").attr("class", theme);        
        
				bgcolor = $(xml).find("bgcolor").text();
				$("body").css("background-color", bgcolor);
        
				wallpaper = $(xml).find("wallpaper").text();
				wallpaper = "url(" + wallpaper + ")";                  
				// set the desktop space

				$("#desktop-space").css("width", $("#desktopSizeX").val()+"px");
				$("#desktop-space").css("height",$("#desktopSizeY").val()+"px");   
				$("#desktop-space").css("background-image", wallpaper); 
				
				
				
				// make changes to the css for RTL
				
				if (rtl) 
				{
					//$("body *").css("float","right");
					//xOrigin = "right";
					$("#kicker").css("float","right");
					$("#connectStatus").css("float","left");
				}
			}                    
		});  
		
		//test
		//$("#testwindow").draggable();   
	}
	
	//----------------------------------------------- Start App -------------------------------------------//
    
	$().initDesktop();
	
	// Initialise comet comms
	

	
	// setWindowFront($(selectedWindow)).parent());
	

	

	

	
	
	// easing equations included here for now - no need for all of them at the moment

	function easeOutElastic(x, t, b, c, d) {
		var s=1.70158;var p=0;var a=c;
		if (t==0) return b;  if ((t/=d)==1) return b+c;  if (!p) p=d*.3;
		if (a < Math.abs(c)) { a=c; var s=p/4; }
		else var s = p/(2*Math.PI) * Math.asin (c/a);
		return a*Math.pow(2,-10*t) * Math.sin( (t*d-s)*(2*Math.PI)/p ) + c + b;
	}

	function easeOutQuad(x, t, b, c, d) {
		return -c *(t/=d)*(t-2) + b;
	}
	

            

                               
	//$("#desktop-icons").load("system/desktop/icons.html", setupIcons); 
     
	//AW4 Webtop    
	//$.getScript("system/desktop/setup.icons.js");
	$.getScript("/webtop/js/icons/setup.icons.js");
    
	// setup kicker and kickoff
	var closer;    
	$("#startmenu").fadeOut();
	
	$("#startmenu").load("apps/kickoff/kickoff.html",function()
	{
		$("#logout").text($("#logoutLabel").val());   
		
		if (rtl) 
		{
			$("#startmenu").removeClass("ltr").addClass("rtl");
			$("#attribution").removeClass("ltr").addClass("rtl");				
		}

	});
	
	
	$("#kicker").fadeTo("fast", 0.7);

	$("#kicker").hover(function(){
		$(this).fadeTo("fast", 1);
		if(closer) clearTimeout(closer);
	},
	function() {
		$(this).fadeTo("fast", 0.7);
		closer=setTimeout('$("#startmenu, #attribution").fadeOut();', 3000);
	});

	$("#kicker").click(function(){
		$("#startmenu").fadeIn();    
	});

	$("#startmenu").hover(function(){
		if(closer) clearTimeout(closer);
	},function() {
		$("#startmenu").fadeOut();
	});

	$("#kicker").rightClick(function(){
		$("#attribution").fadeIn();    
	});

	// Clicking on the desktop deselects all icons and finishes rename icon if appropiate
	$("#desktop-space").click(function()
	{
		updateIconTextIfChanged();
		$(".icon").removeClass("icon-focus");
		$("#icon-menu").fadeOut("fast");
		$("#window-menu").fadeOut("fast");
		//_cometd.publish(channelId, {'target': '~/Desktop', 'userEvent': {'action': 'Select'}});
		$.comms.publish({'target': '~/Desktop', 'userEvent': {'action': 'Select'}});
	}); 
	
	// Restore Default Desktop
		
	$("#restore").live("click",function() {
		//_cometd.publish(channelId, {'target': '~/', 'action': {'action': 'RestoreDefaultDesktop'}});
		$.comms.publish({'target': '~/', 'userEvent': {'action': 'RestoreDefaultDesktop'}});
		return(false);  
	});	
	
	
	$(".lang-select").live("click", function()
	{
		var lang = $(this).val();
		//_cometd.publish(channelId, {'target': '~/Desktop', 'userEvent': {'action': 'Language','language': lang}});
		$.comms.publish({'target': '~/Desktop', 'userEvent': {'action': 'Language','language': lang}});
		return false;
	});	
	
	// Keypress detection
	
	$(document).keypress(function (e) 
	{
		
		// Delete Icon
		var id = $('.icon-focus').attr('id');
		
      if ((e.which == 8 || e.which == 0) && $("#txt"+id).hasClass("icon-text"))
      {
			//_cometd.publish(channelId, {'target': '~/Desktop/'+$('.icon-focus').text(), 'userEvent': {'action': 'DeleteIcon'}});
			$.comms.publish({'target': '~/Desktop/'+$('.icon-focus').text(), 'userEvent': {'action': 'DeleteIcon'}});
			
			$('.icon-focus').fadeTo("fast", 0, function()
			{
				$('.icon-focus').remove();
			});      	
      }
      
      // Rename Icon (detect return)
      
      if (e.which == 13)
      {
      	updateIconTextIfChanged();
      }
      //return (false);
	});
	
	function updateIconTextIfChanged()
	{
		var id = $('.icon-focus').attr('id');
		if ($("#txt"+id).hasClass("icon-text-edit"))
		{
			var newName = $("#txt"+id+" input").val();
			var originalName = $("#txt"+id).data("originalName");

			$("#txt"+id).html(newName).removeClass("icon-text-edit").addClass("icon-text");
			
			if (newName != originalName)
			{
				//_cometd.publish(channelId, {'target': '~/Desktop/'+originalName, 'userEvent': {'action': 'RenameIcon', 'newName': newName}});
				$.comms.publish({'target': '~/Desktop/'+originalName, 'userEvent': {'action': 'RenameIcon', 'newName': newName}});
			}
		}
	}

	
});
  
$(window).load(function() {
	$("#loader").fadeOut();

	$("#desktop-holder").css("height",($("#taskbar").position().top)+"px");
	
	//experimenting
	/*if ($("#desktop-holder").css("width").replace("px","") >= $("#desktopSizeX").val())
	{
		$("#desktop-holder").css("overflow","hidden");
	}*/
	
	//$("#desktop-space").draggable();
	
	//alert($("#desktop-holder").css("height"));
	//alert($("#desktop-holder").css("width"));
	//setTimeout('$("#loader").fadeOut();', 2000);  

});

$(window).resize(function(){
  $("#desktop-holder").css("height",($("#taskbar").position().top)+"px");
});

