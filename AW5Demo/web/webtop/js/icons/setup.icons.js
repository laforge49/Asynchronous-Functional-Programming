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


$(document).ready(function() {


  $("#home-starter").live("click", function(){
    $("#nos").window({ url: "nos/nos.html" });          
  });
  
  /*$("#oxipad-starter").click(function() {
      $("#oxipad").window({ url: "oxipad/oxipad.php" });            
  });
  
  $("#oxiphoto-starter").click(function() {
      $("#oxiphoto").window({ url: "oxiphoto/oxiphoto.php", width: 520, height: 550 });            
  });
  
  $("#info-starter").click(function() {
      $("#info").window({ url: "about/about.html" });            
  });*/
});
