String.prototype.format = function(args) {
	var result = this;
	if (arguments.length > 0) {
		for (var i = 0; i < arguments.length; i++) {
			if (arguments[i] != undefined) {
				var reg = new RegExp("({[" + i + "]})", "g");
				result = result.replace(reg, arguments[i]);
			}
		}
	}
	return result;
};

function centerModals() {
	$('.modal').each(function(i) {
		var $clone = $(this);
		var display = $clone.css('display');
		if (display == "none") {
			$clone.css('display', 'block');
		}
		var modalHeight = $clone.find('.modal-content').height();
		var width = $clone.find('.modal-content').width();
		var top = Math.round(($clone.height() - modalHeight) / 2);
		top = top > 0 ? top : 0;
		$clone.css('display', display);
		$(this).find('.modal-content').css("margin-top", top);
		$(this).find('.modal-mark').css("height", modalHeight + 20).css("width", width + 20);
	});
}

$('.modal').on('show.bs.modal', centerModals);
$(window).on('resize', centerModals);

util.lrFixFooter("#allFooter");

$(function() {

	/*var speed = 8000;
	var newstoplist = jQuery("#newsList p");
	var sumCount = jQuery("#newsList p").length;
	function Marquee() {
		var ele = $("#newsList p:eq(0)");
		ele.slideUp(2000, function() {
			ele.appendTo("#newsList");
			ele.show();
		});
	}
	var MyMar = setInterval(Marquee, speed);
	newstoplist.onmouseover = function() {
		clearInterval(MyMar);
	};
	newstoplist.onmouseout = function() {
		MyMar = setInterval(Marquee, speed);
	};*/
	$(".leftmenu-folding").on("click", function() {
		var that = $(this);
		$("." + that.data().folding).slideToggle("fast");
	});
	$(".help_list li").mousemove(function() {
		var long = 104;
		$(this).data("long") && (long = 134), $(this).find("span").stop().animate({
			width : long + "px"
		}, 100);
	}).mouseout(function() {
		$(this).find("span").stop().animate({
			width : "0px"
		}, 100);
	});
    var wallet = function() {
        util.network({
                         url : "/real/userWallet.html",
                         param : {},
                         success : function(data) {
                             if (data.code == 200) {
                                 if ($("#assetsDetail").length > 0) {
                                     var html = "";
                                     $.each(data.data.wallet, function(key, value) {
                                         html += '<ul id="headercoin' + value.id + '" class="clearfix">';
                                         html += '<li class="col-xs-4 padding-left-clear">' + value.shortName + '</li>';
                                         html += '<li class="col-xs-4 text-center text-danger">' + util.numFormat(value.total, util.DEF_COIN_SCALE) + '</li>';
                                         html += '<li class="col-xs-4 text-center">' + util.numFormat(value.frozen, util.DEF_COIN_SCALE) + '</li>';
                                         html += '</ul>';
                                     });
                                     $("#assetsDetail").append(html);
                                 }
                             }
                         },
                     });
    }
    if ($("#assetsDetail").length > 0) {
        wallet();
    }
    $(".lan-tab-hover").on("click", function() {
        if ("undefined" == typeof (this.id)) {
            return;
        }
        var lan = $(this).data().lan;
        if (lan == this.id) {
            return;
        }
        util.network({
                         url : "/real/switchlan.html",
                         param : {
                             lan : this.id
                         },
                         success : function(data) {
                             if (data.code == 200) {
                                 window.location.reload();
                             }
                         },
                     });
    });
});