package com.kilikili.entity.constants;

public class Constants {
    //password正则表达式(6-20位)
    public static final String REGEX_PASSWORD =  "^[a-zA-Z0-9]{6,20}$";//密码正则表达式(6-20位)

    public static final Integer  REDIS_KEY_EXPIRES_ONE_MIN = 60000;
    public static final String REDIS_KEY_PREFIX = "kilikili:";
    public static final long REDIS_KEY_EXPIRES_ONE_DAY = 86400000 ;
    public static  String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREFIX + "checkCode:";
    
    // 长度常量
    public static final Integer LENGTH_5 = 5;
    public static final Integer LENGTH_10 = 10;
    public static String REDIS_KEY_Token_Web= REDIS_KEY_PREFIX + "token:web:";
    public static String REDIS_KEY_Token_Admin= REDIS_KEY_PREFIX + "token:admin:";
}
