var reg = {
    checkUserName: function () {
        var regType = $("#regType").val();
        var regUserName = '';
        var desc = "";
        if (regType == 1) {
            regUserName = util.trim($("#register-email").val());
            if (regUserName.indexOf(" ") > -1) {
                desc = util.getLan("user.tips.5");
            } else if (regUserName == '') {
                desc = util.getLan("user.tips.6");
            } else if (!util.checkEmail(regUserName)) {
                desc = util.getLan("user.tips.7");
            } else if (new RegExp("[,]", "g").test(regUserName)) {
                desc = util.getLan("user.tips.8");
            } else if (regUserName.length > 100) {
                desc = util.getLan("user.tips.9");
            }
            if (desc != "") {
                util.layerTips("register-email", desc);
                return;
            }
        }else if(regType == 0){
            regUserName = util.trim($("#register-phone").val());
            if (regUserName.indexOf(" ") > -1) {
                desc = util.getLan("user.tips.2");
            } else if (regUserName == '') {
                desc = util.getLan("user.tips.4");
            } else if (!util.checkNumberInt(regUserName)) {
                desc = util.getLan("user.tips.0");
            }
            if (desc != "") {
                util.layerTips("register-phone", desc);
                return;
            }
        }else{
            alert("server error ,please refresh current page！");
            return
        }

        var url = "/user/check_user_exist.html";
        var param = {
            name: regUserName,
            type: regType
        };
        var callback = function (data) {
            if (data.code == -1) {
                util.layerTips(regType == 1?"register-email":"register-phone", data.msg);
            }
        };
        util.network({url: url, param: param, success: callback});
    },
    checkPassword: function () {
        var pwd = util.trim($("#register-password").val());
        var desc = util.isPassword(pwd);
        if (desc != "") {
            util.layerTips("register-password", desc);
            return false;
        }
        return true;
    },
    checkRePassword: function () {
        var pwd = util.trim($("#register-password").val());
        var rePwd = util.trim($("#register-confirmpassword").val());
        var desc = util.isPassword(pwd);
        if (desc != "") {
            util.layerTips("register-confirmpassword", desc);
            return false;
        }
        if (pwd != rePwd) {
            util.layerTips("register-confirmpassword", util.getLan("user.tips.10"));
            return false;
        }
        return true;
    },
    checkUserNameNoJquery: function () {
        var regType = $("#regType").val();
        var regUserName = '';
        var desc = '';
        if (regType == 1) {
            regUserName = util.trim($("#register-email").val());
            if (regUserName.indexOf(" ") > -1) {
                desc = util.getLan("user.tips.5");
            } else if (regUserName == '') {
                desc = util.getLan("user.tips.6");
            } else if (!util.checkEmail(regUserName)) {
                desc = util.getLan("user.tips.7");
            } else if (new RegExp("[,]", "g").test(regUserName)) {
                desc = util.getLan("user.tips.8");
            } else if (regUserName.length > 100) {
                desc = util.getLan("user.tips.9");
            }
        }else if(regType == 0){
            regUserName = util.trim($("#register-phone").val());
            if (regUserName.indexOf(" ") > -1) {
                desc = util.getLan("user.tips.2");
            } else if (regUserName == '') {
                desc = util.getLan("user.tips.4");
            } else if (!util.checkNumberInt(regUserName)) {
                desc = util.getLan("user.tips.0");
            }
            if (desc != "") {
                util.layerTips("register-phone", desc);
                return;
            }
        }else{
            alert("server error ,please refresh current page！");
            return
        }
        if (desc != "") {
            util.layerTips("register-email", desc);
            return false;
        }
        return true;
    },
    register: function (ele) {
        var regType = $("#regType").val();
        var flag = this.checkUserNameNoJquery();
        if (!flag) {
            return;
        }
        if (!this.checkPassword()) {
            return;
        }
        if (!this.checkRePassword()) {
            return;
        }

        var regUserName = '';
        if(regType == 1){
            regUserName = util.trim($("#register-email").val());
        }else{
            regUserName = util.trim($("#register-phone").val());
        }
        var validateCode = $("#register-imgcode").val();
        if (validateCode == "") {
            util.layerTips("register-imgcode", util.getLan("user.tips.11"));
            return;
        }
        var pwd = util.trim($("#register-password").val());
        var regEmailCode = '';
        if (regType == 1){
            regEmailCode = $("#register-email-code").val();
        }else{
            regEmailCode = $("#register-phone-areacode").val();
        }
        if (regEmailCode == "") {
            if(regType == 1) {
                util.layerTips("register-email-code", util.getLan("user.tips.13"));
            }else{
                util.layerTips("register-phone-areacode", util.getLan("user.tips.13"));
            }
            return;
        }
        if (!document.getElementById("agree").checked) {
            util.layerTips("agreeBox", util.getLan("user.tips.14"), true);
            return;
        }
        var intro_user = $("#register-intro").val();
        var url = "/register.html";
        var area = $.trim($("#form-site").attr('select-data'));
        var param = {
            regName: regUserName,
            password: pwd,
            regType: regType,
            vcode: validateCode,
            ecode: regEmailCode,
            pcode: regEmailCode,
            intro_user: intro_user,
            areaCode:area
        };
        var callback = function (data) {
            if (data.code != 200) {
                // 注册失败
                var tipsEle = "";
                switch (data.code) {
                    case -10:
                        tipsEle = "register-password";
                        break;
                    case -20:
                        tipsEle = "register-imgcode";
                        break;
                    case -30:
                        tipsEle = "register-intro";
                        break;
                    case -40:
                        tipsEle = "register-phone";
                        break;
                    case -50:
                        tipsEle = "register-msgcode";
                        break;
                    case -60:
                        tipsEle = "register-email";
                        break;
                    case -70:
                        tipsEle = "register-email-code";
                        break;
                }
                if (tipsEle != "") {
                    util.layerTips(tipsEle, data.msg);
                } else {
                    util.layerAlert(tipsEle, data.msg, 2);
                }
                if (data.code == -20) {
                    $("#register-imgcode").val("");
                    $(".btn-imgcode").click();
                }
            } else {
                util.layerAlert("", util.getLan("user.tips.35"), 1, function () {
                    window.location = '/user/security.html';
                });
            }
        };
        util.network({btn: ele, url: url, param: param, success: callback});
    }
};
$(function () {
    $(".btn-sendmsg").on("click", function () {
        var areacode = $("#register-phone-areacode").html();
        var phone = $("#register-phone").val();
        var imgcode = $("#register-imgcode").val();
        var tipElement_id = {
            phone: $(this).data().tipsid0,
            imgcode: $(this).data().tipsid1
        }
        newMsg.sendMsgCode($(this).data().msgtype, tipElement_id, this.id, areacode, phone, imgcode);
    });
    $(".btn-imgcode").on("click", function () {
        this.src = "/servlet/ValidateImageServlet.html?r=" + Math.round(Math.random() * 100);
    });
    $("#register-phone").on("blur", function () {
        //console.log('phone');
        reg.checkUserName();
    });
    $("#register-email").on("blur", function () {
        reg.checkUserName();
    });
    $("#register-submit").on("click", function () {
        reg.register(this);
    });
    $(".register-item").on("click", function () {
        that = $(this);
        var className = that.attr("class");
        if (className.indexOf("active") >= 0) {
            return;
        }
        $(".register-item").removeClass("active");
        that.addClass("active");
        $("." + that.data().show).show();
        $("." + that.data().hide).hide();
        $("#regType").val(that.data().type);
    });
    $("#register-areaCode").on("change", function () {
        reg.areaCodeChange(this, "register-phone-areacode");
    });

    $(".btn-sendemailcode").on("click", function () {
        var address = $("#register-email").val();
        if (address == "") {
            util.layerTips($(this).data().tipsid, "请输入邮箱地址");
            return;
        }
        if (!util.checkEmail(address)) {
            util.layerTips($(this).data().tipsid, language["comm.error.tips.13"]);
            return;
        }
        newEmail.sendcode($(this).data().msgtype, $(this).data().tipsid, this.id, address);
    });

    $(".btn-sendphonecode").on("click", function () {
        var phone = $("#register-phone").val();
        var area = $.trim($("#form-site").attr('select-data'));
        if (phone == "") {
            util.layerTips($(this).data().tipsid, "请输入手机号");
            return;
        }
        if (area == ""){

            return;
        }
        if (!util.checkNumber(phone)) {
            util.layerTips($(this).data().tipsid, language["comm.error.tips.13"]);
            return;
        }
        PhoneMsg.sendcode($(this).data().msgtype, $(this).data().tipsid, this.id, phone,area);
    });
});