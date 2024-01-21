var news = {
	loadMore : function() {
		if ($("#nextpage").length <= 0) {
			return;
		}
		var cur = $("#nextpage").val();
		var id = $("#id").val();
		var url = "/service/appnewmore.html?id=" + id + "&cur=" + cur;
		var callback = function(data) {
			if (data.code == 0) {
				var html = "";
				$.each(data.items, function(key, value) {
					html += '<a href="/service/appnew.html?id=' + value.id + '">';
					html += '<div class="media clearfix">';
					html += '<div class="media-body">';
					html += '<span class="news-title">';
					html += '<span>' + value.title + '</span>';
					html += '</span>';
					html += '<p class="date">';
					html += '<i class="icon"></i>官方公告<i class="split"></i>' + value.date + '</p>';
					html += '</div>';
					html += '<div class="media-right">';
					html += '<img alt="" src="' + value.img + '">';
					html += '</div>';
					html += '</div>';
					html += '</a>';
				});
				if (html != "") {
					$("#newsitem").append(html);
				}
				if (data.nextpage == 0) {
					$("#newsmore").remove();
				} else {
					$("#nextpage").val(data.nextpage);
				}
			}
		};
		$.get(url,callback,"json");
		/*util.network({
			url : url,
			method : "get",
			success : callback,
		});*/
	},
	carousel : function() {
		// 轮播图
		var $allItems = $(".index_carousel .index_carousel-inner .item");
		var $allIndicators = $(".index_carousel .index_carousel-indicators li");
		var currentIndex = 0;
		var currentItem = null;
		var nextItem = null;
		var time;
		var functionbuttonhover = true;
		$(".index_carousel").hover(function() {
			time = window.clearInterval(time);
		}, function() {
			time = setInterval(function() {
				currentItem = $allItems.filter('.active');
				if (currentIndex + 1 === $allItems.length) {
					nextItem = $allItems.eq(0);
					currentIndex = 0;
				} else {
					nextItem = $allItems.eq(currentIndex + 1);
					currentIndex += 1;
				}
				nextItem.addClass('active').fadeIn(500);
				$allIndicators.removeClass('active').eq(currentIndex).addClass('active');
				currentItem.removeClass('active').fadeOut(1000);
			}, 5000);
		}).trigger("mouseleave");
		$(".index_carousel-indicators li").click(function() {
			var nextIndex = parseInt(jQuery(this).attr('data-slide-to'));
			if (nextIndex == currentIndex)
				return false;
			currentIndex = nextIndex;
			currentItem = $allItems.filter('.active');
			currentItem.removeClass('active').fadeOut(1000);
			$allItems.eq(currentIndex).addClass('active').fadeIn(500);
			$allIndicators.removeClass('active').eq(currentIndex).addClass('active');
		});
	}
};
$(function() {
	/*news.carousel();*/
	$("#newsmore").on('click', function() {
		news.loadMore();
	});
});