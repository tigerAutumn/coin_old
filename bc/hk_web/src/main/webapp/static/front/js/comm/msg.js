var email = {
	secs : 121,
	msgtype : 1,
	sendcode : function(type, tipElement_id, button_id, address) {
		var that = this;
		var tipElement = document.getElementById(tipElement_id);
		var button = document.getElementById(button_id);
		if (typeof (address) == 'undefined') {
			address = 0;
		} else {
			if (!util.checkEmail(address)) {
				util.showerrortips(tipElement_id, language["msg.tips.6"]);
				return;
			}
		}
		var url = "/user/send_reg_email.html";
		var param = {
			type : type,
			msgtype : this.msgtype,
			address : address
		};
		var callback = function(data) {
			if (data.code == 200) {
				button.disabled = true;
				for (var num = 1; num <= that.secs; num++) {
					window.setTimeout("email.updateNumber(" + num + ",'" + button_id + "',2)", num * 1000);
				}
			} else {
				util.showerrortips(tipElement_id, data.msg);
			}
		};
		util.network({
			btn : button,
			url : url,
			param : param,
			success : callback,
		});
	},
	updateNumber : function(num, button_id, isVoice) {
		var button = document.getElementById(button_id);
		if (num == this.secs) {
			button.innerHTML = language["msg.tips.3"];
			button.disabled = false;
		} else {
			var printnr = this.secs - num;
			button.innerHTML = language["msg.tips.4"].format(printnr);
		}
	},
    sendMsgCode:function(type, tipElement_id, button_id){
        var that = this;
        var tipElement = document.getElementById(tipElement_id);
        var button = document.getElementById(button_id);

        var url = "/user/send_bank_sms.html";
        var param = {};
        var callback = function(data) {
            if (data.code == 200) {
                button.disabled = true;
                for (var num = 1; num <= that.secs; num++) {
                    window.setTimeout("email.updateNumber(" + num + ",'" + button_id + "',2)", num * 1000);
                }
            } else {
                util.showerrortips(tipElement_id, data.msg);
            }
        };
        util.network({
            btn : button,
            url : url,
            param : param,
            success : callback,
        });
	}
};
var newEmail = {
	secs : 121,
	msgtype : 1,
	sendcode : function(type, tipElement_id, button_id, address) {
		var that = this;
		var tipElement = document.getElementById(tipElement_id);
		var button = document.getElementById(button_id);
		if (typeof (address) == 'undefined') {
			address = 0;
		} else {
			if (!util.checkEmail(address)) {
				util.layerTips(tipElement_id, language["msg.tips.6"]);
				return;
			}
		}
		var url = "/user/send_reg_email.html";
		var param = {
			type : type,
			msgtype : this.msgtype,
			address : address
		};
		var callback = function(data) {
			if (data.code == 200) {
				button.disabled = true;
				for (var num = 1; num <= that.secs; num++) {
					window.setTimeout("email.updateNumber(" + num + ",'" + button_id + "',2)", num * 1000);
				}
			} else {
				util.layerTips(tipElement_id, data.msg);
			}
		};
		util.network({
			btn : button,
			url : url,
			param : param,
			success : callback,
		});
	},
	updateNumber : function(num, button_id, isVoice) {
		var button = document.getElementById(button_id);
		if (num == this.secs) {
			button.innerHTML = language["msg.tips.3"];
			button.disabled = false;
		} else {
			var printnr = this.secs - num;
			button.innerHTML = language["msg.tips.4"].format(printnr);
		}
	}
};

var PhoneMsg = {
    secs : 121,
    msgtype : 1,
    sendcode : function(type, tipElement_id, button_id, phone,areacode) {
        var that = this;
        var tipElement = document.getElementById(tipElement_id);
        var button = document.getElementById(button_id);
        if (typeof (phone) == 'undefined') {
            phone = 0;
        } else {
            if (!util.checkNumberInt(phone)) {
                util.layerTips(tipElement_id, language["msg.tips.6"]);
                return;
            }
        }
        var url = "/user/send_sms.html";
        var param = {
            type : type,
            msgtype : this.msgtype,
			area:areacode,
            phone : phone
        };
        var callback = function(data) {
            if (data.code == 200) {
                button.disabled = true;
                for (var num = 1; num <= that.secs; num++) {
                    window.setTimeout("email.updateNumber(" + num + ",'" + button_id + "',2)", num * 1000);
                }
            } else {
                util.layerTips(tipElement_id, data.msg);
            }
        };
        util.network({
            btn : button,
            url : url,
            param : param,
            success : callback,
        });
    },
    updateNumber : function(num, button_id, isVoice) {
        var button = document.getElementById(button_id);
        if (num == this.secs) {
            button.innerHTML = language["msg.tips.3"];
            button.disabled = false;
        } else {
            var printnr = this.secs - num;
            button.innerHTML = language["msg.tips.4"].format(printnr);
        }
    }
};

var BindPhoneMsg = {
    secs : 121,
    msgtype : 1,
    sendcode : function(tipElement_id, button_id, phone,areacode) {
        var that = this;
        var tipElement = document.getElementById(tipElement_id);
        var button = document.getElementById(button_id);
        if (typeof (phone) == 'undefined') {
            phone = 0;
        } else {
            if (!util.checkNumberInt(phone)) {
                util.layerTips(tipElement_id, language["msg.tips.6"]);
                return;
            }
        }
        var url = "/validate/send_bindphone_sms.html";
        var param = {
            area:areacode,
            phone : phone
        };
        var callback = function(data) {
            if (data.code == 200) {
                button.disabled = true;
                for (var num = 1; num <= that.secs; num++) {
                    window.setTimeout("email.updateNumber(" + num + ",'" + button_id + "',2)", num * 1000);
                }
            } else {
                util.layerTips(tipElement_id, data.msg);
            }
        };
        util.network({
            btn : button,
            url : url,
            param : param,
            success : callback,
        });
    },
    updateNumber : function(num, button_id, isVoice) {
        var button = document.getElementById(button_id);
        if (num == this.secs) {
            button.innerHTML = language["msg.tips.3"];
            button.disabled = false;
        } else {
            var printnr = this.secs - num;
            button.innerHTML = language["msg.tips.4"].format(printnr);
        }
    }
};

var FindPhoneMsg = {
    secs : 121,
    msgtype : 1,
    sendMsgCode : function(tipElement_id, button_id, phone,areacode,imgcode) {
        var that = this;
        var tipElement = document.getElementById(tipElement_id);
        var button = document.getElementById(button_id);
        if (typeof (phone) == 'undefined') {
            phone = 0;
        } else {
            if (!util.checkNumberInt(phone)) {
                util.layerTips(tipElement_id, language["msg.tips.6"]);
                return;
            }
        }
        var url = "/validate/sendFindPhoneCode.html";
        var param = {
            area:areacode,
            phone:phone,
            imgcode:imgcode
        };
        var callback = function(data) {
            if (data.code == 200) {
                button.disabled = true;
                for (var num = 1; num <= that.secs; num++) {
                    window.setTimeout("email.updateNumber(" + num + ",'" + button_id + "',2)", num * 1000);
                }
            } else {
                util.layerTips(tipElement_id, data.msg);
            }
        };
        util.network({
            btn : button,
            url : url,
            param : param,
            success : callback,
        });
    },
    updateNumber : function(num, button_id, isVoice) {
        var button = document.getElementById(button_id);
        if (num == this.secs) {
            button.innerHTML = language["msg.tips.3"];
            button.disabled = false;
        } else {
            var printnr = this.secs - num;
            button.innerHTML = language["msg.tips.4"].format(printnr);
        }
    }
};