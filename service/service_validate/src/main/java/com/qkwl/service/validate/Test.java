//package com.qkwl.service.validate;
//
//import com.qkwl.service.validate.impl.ValidateServiceImpl;
//import com.qkwl.service.validate.model.ValidateAccountDO;
//import com.qkwl.service.validate.model.ValidateSmsDO;
//
//public class Test {
//
//    public static void main(String args[]){
//
//        ValidateAccountDO va = new ValidateAccountDO();
//        va.setSecretKey("4afIxUyz8Xd44d");
//        va.setAccessKey("I4010252");
//        va.setUrl("http://222.73.117.140:8044/mt");
//
//        ValidateSmsDO vs = new ValidateSmsDO();
//        vs.setPhone("8615823488981");
//        vs.setContent("尊敬的用户，您本次的验证码为1111有效期10分钟");
//        ValidateServiceImpl.send253InternationalSms(va,vs);
//
//
//    }
//}
