$(".lan-tab-hover").on({
    click:function(){
        var _text = $(this).children('i').attr("class");
        $(this).parent(".lan-switch").siblings('.lan-tab').children('i').removeClass().addClass(_text);
    }
})


$(".header-nav-btn").on({
    click:function(){
        $(".header-nav-btn").removeClass("active");
        $(this).addClass("active");
    }
})



$(".form-site").on({
    click:function(){
        $(".form-site-list").fadeToggle(400);
        if($(this).children("i").hasClass("icon-xia")){
            $(this).children("i").removeClass("icon-xia").addClass("icon-shang");
        }else{
            $(this).children("i").addClass("icon-xia").removeClass("icon-shang");
        }
    }
})


$(".form-site-item").on({
    click:function(){
        var _text = $(this).html();
        $(this).parents(".form-site-list").fadeOut(400);
        $(this).parents(".form-site-list").siblings(".form-site").children("span").html(_text);
        $(this).parents(".form-site-list").siblings(".form-site").children("i").addClass("icon-xia").removeClass("icon-shang");
        $(this).parents(".form-site-list").siblings(".form-site").attr("select-data",$(this).attr("code"));
    }
})
