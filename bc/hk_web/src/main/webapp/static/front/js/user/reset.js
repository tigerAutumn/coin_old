var reset = {
    findPassEmail: function (btnele) {
        var email = $("#reset-email").val();
        var idcard = $("#reset-idcard").val();
        var idcardno = $("#reset-idcardno").val();
        var imgcode = $("#reset-imgcode").val();
        if (email == "" || !util.checkEmail(email)) {
            util.layerAlert("", util.getLan("user.tips.18"));
            return;
        }
        if (imgcode == "" || imgcode.length != 4) {
            util.layerAlert("", util.getLan("user.tips.16"));
            return;
        }
        var url = "/validate/send_findbackmail.html";
        var param = {
            email: email,
            idcard: idcard,
            idcardno: idcardno,
            imgcode: imgcode
        };
        var callback = function (data) {
            if (data.code == 200) {
                util.layerAlert("", util.getLan("user.tips.19"), 1);
            } else {
                util.layerAlert("", data.msg, 2);
            }
        };
        util.network({btn: btnele, url: url, param: param, success: callback});
    },
    resetEmailPass: function (btnele) {
        var regu = /^[0-9]{6}$/;
        var re = new RegExp(regu);
        var totpCode = 0;
        var newPassword = $("#reset-newpass").val();
        var newPassword2 = $("#reset-confirmpass").val();
        var fid = $("#fid").val();
        var uuid = $("#uuid").val();
        var msg = util.isPassword(newPassword);
        if (msg != "") {
            util.layerAlert("", msg);
            $("#reset-newpass").val("");
            return;
        }
        if (newPassword != newPassword2) {
            util.layerAlert("", util.getLan("user.tips.17"));
            $("#reset-confirmpass").val("");
            return;
        }
        if ($("#reset-googlecode").length > 0) {
            totpCode = util.trim($("#reset-googlecode").val());
            if (!re.test(totpCode)) {
                util.layerAlert("", util.getLan("comm.tips.2"));
                return;
            }
        }
        var url = "/validate/reset_password.html";
        var param = {
            totpCode: totpCode,
            newPassword: newPassword,
            newPassword2: newPassword2,
            fid: fid
        };
        var callback = function (data) {
            if (data.code == 200) {
                $("#secondstep").hide();
                $("#successstep").show();
                $("#resetprocess2").removeClass("active");
                $("#resetprocess3").addClass("active");
            } else {
                util.layerAlert("", data.msg);
                if (data.code == -3) {
                    $("#reset-confirmpass").val("");
                }
                if (data.code == -4) {
                    $("#reset-newpass").val("");
                    $("#reset-confirmpass").val("");
                }
                if (data.code == -8) {
                    $("#totpCode").val("");
                }
            }
        };
        util.network({btn: btnele, url: url, param: param, success: callback});
    },
    checkPhoneCode:function (btnele) {
        var area =  $("#reset-phone-areacode").html();
        var phone = $("#reset-phone").val();
        var code = $("#reset-msgcode").val();
        var idcardno = $("#reset-idcardno").val();
        var url = "/validate/reset_password_check.html";
        var param = {
            area:area,
            phone:phone,
            code:code,
            idcardno:idcardno
        };
        var callback = function (data) {
            if (data.code == 200) {
                window.location.href = '/validate/reset_phone.html';
            } else {
                util.layerAlert("", data.msg);
            }
        };
        util.network({btn: btnele, url: url, param: param, success: callback});
    },
    resetPasswdPhone:function (btnele) {
        var regu = /^[0-9]{6}$/;
        var re = new RegExp(regu);
        var totpCode = 0;
        var newPassword = $("#reset-newpass").val();
        var newPassword2 = $("#reset-confirmpass").val();
        var msg = util.isPassword(newPassword);
        if (msg != "") {
            util.layerAlert("", msg);
            $("#reset-newpass").val("");
            return;
        }
        if (newPassword != newPassword2) {
            util.layerAlert("", util.getLan("user.tips.17"));
            $("#reset-confirmpass").val("");
            return;
        }
        if ($("#reset-googlecode").length > 0) {
            totpCode = util.trim($("#reset-googlecode").val());
            if (!re.test(totpCode)) {
                util.layerAlert("", util.getLan("comm.tips.2"));
                return;
            }
        }
        var url = "/validate/reset_password_phone.html";
        var param = {
            totpCode: totpCode,
            newPassword: newPassword,
            newPassword2: newPassword2,
        };
        var callback = function (data) {
            if (data.code == 200) {
                $("#secondstep").hide();
                $("#successstep").show();
                $("#resetprocess2").removeClass("active");
                $("#resetprocess3").addClass("active");
            } else {
                util.layerAlert("", data.msg);
                if (data.code == -3) {
                    $("#reset-confirmpass").val("");
                }
                if (data.code == -4) {
                    $("#reset-newpass").val("");
                    $("#reset-confirmpass").val("");
                }
                if (data.code == -8) {
                    $("#totpCode").val("");
                }
            }
        };
        util.network({btn: btnele, url: url, param: param, success: callback});
    }
};
$(function () {
    $(".btn-imgcode").on("click", function () {
        this.src = "/servlet/ValidateImageServlet.html?r=" + Math.round(Math.random() * 100);
    });
    $(".btn-sendmsg").on("click", function () {
        if (this.id == "reset-sendmessage") {
            var areacode = $("#reset-phone-areacode").html();
            var phone = $("#reset-phone").val();
            var imgcode = $("#reset-imgcode").val();
            FindPhoneMsg.sendMsgCode($(this).data().tipsid, this.id,phone,areacode,imgcode);
        }
    });

    $("#btn-email-next").on("click", function () {
        reset.findPassEmail(this);
    });

    $("#btn-email-success").on("click", function () {
        reset.resetEmailPass(this);
    });

    $("#btn-next").on('click',function () {
        reset.checkPhoneCode(this);
    });

    $("#btn-success").on('click',function () {
        reset.resetPasswdPhone(this);
    })
});