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


var debugSet = 2;

jQuery.debug = function(message,level){

	if (level == null) level = 1;
	if(window.console && debugSet > 0 && debugSet <= level){
		console.debug(message);	
	}
}

jQuery.mirrorIfRtl = function(rtl,x,offset)
{
	if (offset != null) 
	{
		reverseOffset = offset;
	}
	else
	{
		reverseOffset = 128; // default used for icons should be calculated
	}
	if (rtl == true) x = $("#desktopSizeX").val() - reverseOffset - x;
	return(Math.round(x)+"");
}

jQuery.scrollToIcon = function(id)
{
		var offsetX = $("#desktop-holder").width()*-0.5+48;
		var offsetY = $("#desktop-holder").height()*-0.5+48;

		$("#desktop-holder").scrollTo($("#desktop-icons #"+id),1000,{easing:"easeOutQuad", axis:'xy', offset : {left:offsetX,top:offsetY}});
}
	
jQuery.scrollToWindow = function(id)
{
	var self = $("#"+id);
		
	$.debug('selectedWindow = '+id);
		
	if ($(self).length > 0)
	{
		$.debug('scrollToWindow('+id+')');
		var winWidth = self.dialog('option', 'width');
		var winHeight = self.dialog('option', 'height');
		var desktopWidth = $("#desktop-holder").width();
		var desktopHeight = $("#desktop-holder").height();
		 
		$.debug('window width = '+winWidth);
		
		var offsetX = (desktopWidth-winWidth)*-0.5;
		
		// Take into account window dimensions for accurate centering
		// self.prev() is the top of the window
		
		// If the window is larger than teh viewport pin the window to the top - no Y offset required
			
		if (winHeight < desktopHeight)
		{
			var offsetY = (desktopHeight-winHeight)*-0.5;
			
			/*$("#desktop-holder").queue(function()
			{
				$(this).stop(true, true).scrollTo(self.prev(),1000,{easing:"easeOutQuad", axis:'xy', offset : {left:offsetX,top:offsetY}});
				$(this).dequeue();
			});*/
			
			$("#desktop-holder").scrollTo(self.prev(),1000,{easing:"easeOutQuad", axis:'xy', offset : {left:offsetX,top:offsetY}});
			//$("#desktop-holder").stop(true, true).scrollTo(self.prev(),1000,{easing:"easeOutQuad", axis:'xy', offset : {left:offsetX,top:offsetY}});
		}
		else
		{
			
			/*$("#desktop-holder").queue(function()
			{
				$(this).stop(true, true).scrollTo(self.prev(),1000,{easing:"easeOutQuad", axis:'xy', offset : {left:offsetX}});
				$(this).dequeue();
			});*/
			
			$("#desktop-holder").scrollTo(self.prev(),1000,{easing:"easeOutQuad", axis:'xy', offset : {left:offsetX}});
			//$("#desktop-holder").stop(true, true).scrollTo(self.prev(),1000,{easing:"easeOutQuad", axis:'xy', offset : {left:offsetX}});
		}
	}
}

jQuery.setWindowFront = function(window)
{
	var windowZ = $(window).css('z-index');
	var maxZ = 0;
		
	// get max z-index
		
	$('.ui-dialog').each(function() {
		//$.debug($(this).attr('aria-labelledby')+" old z-index = "+$(this).css('z-index'));
		maxZ = Math.max(maxZ, $(this).css('z-index'));
	});

	$('.ui-dialog').each(function() {
		var thisIdx = $(this).css('z-index');
		if (thisIdx > windowZ)
		{
			$(this).css('z-index',thisIdx-1);
		}	
	});	
			
	// Set new z-index	
			
	$(window).css('z-index',maxZ);
}

jQuery.getUrlParam = function (n,s)
{
	n = n.replace(/[\[]/,"\\[").replace(/[\]]/,"\\]");
	var p = (new RegExp("[\\?&]"+n+"=([^&#]*)")).exec(s);
	return (p===null) ? "" : p[1];
}
