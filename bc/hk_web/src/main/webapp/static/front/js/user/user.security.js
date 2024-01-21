var security = {
    loadGoogleAuth: function () {
        var url = "/user/bind_google_device.html";
        var param = null;
        var callback = function (data) {
            if (data.code == 200) {
                if (navigator.userAgent.indexOf("MSIE") > 0) {
                    $('#qrcode').html("").qrcode({text: data.qecode, width: "140", height: "140", render: "table"});
                } else {
                    $('#qrcode').html("").qrcode({text: data.data.qecode, width: "140", height: "140"});
                }
                $("#bindgoogle-key").html(data.data.totpKey);
                $("#bindgoogle-key-hide").val(data.data.totpKey);
            } else {
                util.layerAlert("", data.msg, 2);
            }
        };
        util.network({url: url, param: param, success: callback});
    },
    sendBindEmail: function (ele, email, flag) {
        var desc = '';
        if (email.indexOf(" ") > -1) {
            desc = util.getLan("user.tips.5");
        } else if (email == '') {
            desc = util.getLan("user.tips.6");
        } else if (!util.checkEmail(email)) {
            desc = util.getLan("user.tips.7");
        }
        if (desc != "") {
            util.layerAlert("", desc);
            return;
        }
        var url = "/validate/send_bindmail.html";
        var param = {
            email: email
        };
        var callback = function (data) {
            if (data.code == 200) {
                if (flag) {
                    util.layerAlert('', data.msg, 1);
                } else {
                    util.layerAlert("", data.msg, 1);
                }
            } else {
                if (flag) {
                    util.layerAlert('', data.msg, 2);
                } else {
                    util.layerAlert("", data.msg, 2);
                }
            }
        };
        util.network({btn: ele, url: url, param: param, success: callback});
    },
    saveBindGoogle: function (ele) {
        var code = util.trim($("#bindgoogle-topcode").val());
        var totpKey = $("#bindgoogle-key-hide").val();
        if (!/^[0-9]{6}$/.test(code)) {
            util.layerAlert("", util.getLan("comm.tips.2"));
        }
        var url = "/user/google_auth.html";
        var param = {
            code: code,
            totpKey: totpKey
        };
        var callback = function (data) {
            if (data.code == 200) {
                util.layerAlert("", data.msg, 1);
                window.setTimeout(function () {
                    window.location.href = '/user/security.html';
                }, 1000);
            } else {
                util.layerAlert("", data.msg, 2);
                $("#bindgoogle-topcode").val("");
            }
        };
        util.network({btn: ele, url: url, param: param, success: callback});
    },
    lookBindGoogle: function (ele) {
        var totpCode = util.trim($("#unbindgoogle-topcode").val());
        if (!/^[0-9]{6}$/.test(totpCode)) {
            util.layerAlert("", util.getLan("comm.tips.2"));
        }
        var url = "/user/get_google_key.html";
        var param = {
            totpCode: totpCode
        };
        var callback = function (data) {
            if (data.code == 200) {
                if (navigator.userAgent.indexOf("MSIE") > 0) {
                    $('#unqrcode').qrcode({text: data.data.qecode, width: "140", height: "140", render: "table"});
                } else {
                    $('#unqrcode').qrcode({text: data.data.qecode, width: "140", height: "140"});
                }
                $("#unbindgoogle-key").html(data.data.totpKey);
                $(".unbindgoogle-hide-box").show();
                $(".unbindgoogle-show-box").hide();
                centerModals();
            } else if (data.code == -1) {
                util.layerAlert("", data.msg, 2);
                $("#unbindgoogle-topcode").val("");
            } else {
                util.layerAlert("", data.msg, 2);
            }
        };
        util.network({btn: ele, url: url, param: param, success: callback});
    },
    saveModifyPwd: function (ele, pwdType, istrade) {
        var originPwdEle = "";
        var newPwdEle = "";
        var reNewPwdEle = "";
        var totpCodeEle = "";
        var errorEle = "";
        var identityEle = "";
        if (pwdType == 0) {
            if (istrade) {
                originPwdEle = "#unbindloginpass-oldpass";
                newPwdEle = "#unbindloginpass-newpass";
                reNewPwdEle = "#unbindloginpass-confirmpass";
                totpCodeEle = "#unbindloginpass-googlecode";
            } else {
                originPwdEle = "#bindloginpass-oldpass";
                newPwdEle = "#bindloginpass-newpass";
                reNewPwdEle = "#bindloginpass-confirmpass";
                totpCodeEle = "#bindloginpass-googlecode";
            }
        } else {
            if (istrade) {
                originPwdEle = "#unbindtradepass-oldpass";
                newPwdEle = "#unbindtradepass-newpass";
                reNewPwdEle = "#unbindtradepass-confirmpass";
                totpCodeEle = "#unbindtradepass-googlecode";
            } else {
                originPwdEle = "#bindtradepass-oldpass";
                newPwdEle = "#bindtradepass-newpass";
                reNewPwdEle = "#bindtradepass-confirmpass";
                totpCodeEle = "#bindtradepass-googlecode";
                identityEle = "#bindtradepass-identityno";
            }
        }
        if (istrade) {
            var originPwd = util.trim($(originPwdEle).val());
        }
        var newPwd = util.trim($(newPwdEle).val());
        var reNewPwd = util.trim($(reNewPwdEle).val());
        var newPwdTips = util.isPassword(newPwd);
        var reNewPwdTips = util.isPassword(reNewPwd);
        if (newPwdTips != "") {
            util.layerAlert("", newPwdTips);
            return;
        }
        if (reNewPwdTips != "") {
            util.layerAlert("", reNewPwdTips);
            return;
        }
        if (newPwd != reNewPwd) {
            util.layerAlert("", util.getLan("user.tips.17"));
            $(reNewPwdEle).val("");
            return;
        }
        var totpCode = "";
        var identityCode = "";
        if ($(totpCodeEle).length > 0) {
            totpCode = util.trim($(totpCodeEle).val());
            if (!/^[0-9]{6}$/.test(totpCode)) {
                util.layerAlert("", util.getLan("comm.tips.2"));
                return;
            }
        }
        if ($(totpCodeEle).length <= 0) {
            util.layerAlert("", util.getLan("comm.tips.1"));
            return;
        }
        if (pwdType != 0 && !istrade && $(identityEle).length > 0) {
            identityCode = util.trim($(identityEle).val());
            if (identityCode == "") {
                util.layerAlert("", util.getLan("user.tips.24"));
                return;
            }
        }
        util.hideerrortips(errorEle);
        var url = "/user/modify_passwd.html";
        var param = {
            pwdType: pwdType,
            originPwd: originPwd,
            newPwd: newPwd,
            reNewPwd: reNewPwd,
            totpCode: totpCode,
            identityCode: identityCode
        };
        var callback = function (data) {
            if (data.code == 200) {
                if (istrade) {
                    util.layerAlert("", pwdType == 0 ? util.getLan("user.tips.25") : util.getLan("user.tips.26"), 1);
                } else {
                    util.layerAlert("", pwdType == 0 ? util.getLan("user.tips.27") : util.getLan("user.tips.28"), 1);
                }
            } else {
                util.layerAlert("", data.msg, 2);
            }
        };
        util.network({btn: ele, url: url, param: param, success: callback});
    },
    saveRealName: function (ele) {
        var realname = $("#bindrealinfo-realname").val();
        var address = $("#bindrealinfo-address").find("option:selected").text();
        var identitytype = $("#bindrealinfo-identitytype").val();
        var identityno = $("#bindrealinfo-identityno").val();
        var ckinfo = document.getElementById("bindrealinfo-ckinfo").checked;
        if (!ckinfo) {
            util.layerAlert("", util.getLan("user.tips.29"));
            return;
        }
        if (realname.length > 10 || realname.trim() == "") {
            util.layerAlert("", util.getLan("user.tips.30"));
            return;
        }
        var url = "/real_name_auth.html";
        var param = {
            realname: realname,
            identitytype: identitytype,
            identityno: identityno,
            address: address.trim()
        };
        var callback = function (data) {
            if (data.code == 200) {
                util.layerAlert("", data.msg, 1);
                location.reload();
            } else {
                util.layerAlert("", data.msg, 2);
            }
        };
        util.network({btn: ele, url: url, param: param, success: callback});
    },
    sendPhoneSms: function (ele) {
        var phone = $("#bindphone-phone").val();
        var area  = "86";
        BindPhoneMsg.sendcode("bindphone-errortips","bindphone-send-code",phone,area);
    },
    bindPhone: function (ele) {
        var phone = $("#bindphone-phone").val();
        var area  = "86";
        var code = $("#bindphone-phone-code").val();

        var url = "/validate/bindphone.html";
        var param = {
            phone: phone,
            area: area,
            code:code
        };
        var callback = function (data) {
            if (data.code == 200) {
                util.layerAlert("", data.msg, 1);
                location.reload();
            } else {
                util.layerAlert("", data.msg, 2);
            }
        };
        util.network({btn: ele, url: url, param: param, success: callback});
    },

};
$(function () {
    $(".btn-imgcode").on("click", function () {
        this.src = "/servlet/ValidateImageServlet.html?r=" + Math.round(Math.random() * 100);
    });
    $('#bindgoogle').on("show.bs.modal", function () {
        security.loadGoogleAuth();
    });
    $("#bindemail-Btn").on("click", function () {
        var email = $("#bindemail-email").val();
        security.sendBindEmail(this, email, false);
    });
    $("#bindemail-send").on("click", function () {
        var email = $("#bindemail-send-email").val();
        security.sendBindEmail("", email, true);
    });
    $("#bindgoogle-Btn").on("click", function () {
        security.saveBindGoogle(this);
    });
    $("#unbindgoogle-Btn").on("click", function () {
        security.lookBindGoogle(this);
    });
    $("#unbindloginpass-Btn").on("click", function () {
        security.saveModifyPwd(this, 0, true);
    });
    $("#bindloginpass-Btn").on("click", function () {
        security.saveModifyPwd(this, 0, false);
    });
    $("#unbindtradepass-Btn").on("click", function () {
        security.saveModifyPwd(this, 1, true);
    });
    $("#bindtradepass-Btn").on("click", function () {
        security.saveModifyPwd(this, 1, false);
    });
    $("#bindrealinfo-Btn").on("click", function () {
        security.saveRealName(this);
    });
    $("#bindphone-send-code").on('click',function(){
        security.sendPhoneSms(this);
    });
    $("#bindphone-Btn").on('click',function () {
        security.bindPhone(this);
    });
});