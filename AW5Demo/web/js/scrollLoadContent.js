 $(function()
 {
	$("#nextLink").remove();

	var index = $("#index").val();
	var key = $("#key").val();
	var template = $("#template").val();

	$(window).scroll(function(){
		if  ($(window).scrollTop() == $(document).height() - $(window).height() ){
			loadNextPage();
        }
	});

	function loadNextPage()
	{
		var extraParams = '';
		$('.param').each(function() 
		{
			extraParams += '&'+$(this).attr('name')+'='+$(this).val();
		});
		
		$.ajax({
        	type: "GET",
			url: template+"?index="+index+"&key="+key+extraParams, async: false,
			dataType: "xmlDocument",
			success: function(xml) {
				$('table').append($('tr:not(:first-child)',xml));
				index = $("#index",xml).val();
				key = $("#key",xml).val();
			}
		})
	}
 });
