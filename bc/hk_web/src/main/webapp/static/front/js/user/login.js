var login = {
    checkLoginUserName: function () {
        var uName = $("#login-account").val();
        if (uName == "") {
            util.layerTips("login-account", util.getLan("user.tips.1"));
            return false;
        }
        return true;
    },
    checkLoginPassword: function () {
        var password = $("#login-password").val();
        var des = util.isPassword(password);
        if (des != "") {
            util.layerTips("login-password", des);
            return false;
        }
        return true;
    },
    login: function () {
        if (login.checkLoginUserName() && login.checkLoginPassword()) {
            var url = "/login.html";
            var uName = $("#login-account").val();
            var pWord = $("#login-password").val();
            var longLogin = 0;
            if (util.checkEmail(uName)) {
                longLogin = 1;
            }
            var forwardUrl = "";
            if ($("#forwardUrl") != null) {
                forwardUrl = $("#forwardUrl").val();
            }
            var param = {
                loginName: uName,
                password: pWord,
                type: longLogin
            };
            var callback = function (data) {
                if (data.code != 200) {
                    util.layerTips("login-password", data.msg);
                    $("#login-password").val("");
                } else {
                    if (forwardUrl.trim() == "") {
                        window.location.href = "/index.html";
                    } else {
                        window.location.href = forwardUrl;
                    }
                }
            };
            util.network({btn: $("#login-submit")[0], url: url, param: param, success: callback, enter: true});
        }
    }
};
$(function () {
    $("#login-password").on("focus", function () {
        util.callbackEnter(login.login);
    });
    $("#login-submit").on("click", function () {
        login.login();
    });
});