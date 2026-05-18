package com.kilikili.entity.constants;

public class Constants {
    //password正则表达式
    public static final String REGEX_PASSWORD = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$";//规定规则为6-20位，数字和字母的组合

    public static final Integer  REDIS_KEY_EXPIRES_ONE_MIN = 60000;
    public static final String REDIS_KEY_PREFIX = "kilikili:";
    public static final long REDIS_KEY_EXPIRES_ONE_DAY = 86400000 ;
    public static  String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREFIX + "checkCode:";
    
    // 长度常量
    public static final Integer LENGTH_10 = 10;
    public static String REDIS_KEY_Token_Web= REDIS_KEY_PREFIX + "token:web:";
}
