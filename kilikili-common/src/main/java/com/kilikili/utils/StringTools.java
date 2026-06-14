package com.kilikili.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class StringTools {
    
    /**
     * MD5加密
     */
    public static String encodeByMd5(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("加密字符串不能为空");
        }
        return DigestUtils.md5Hex(str);
    }
    
    /**
     * 生成随机数字
     */
    public static String getRandomNumber(int length) {
        return RandomStringUtils.randomNumeric(length);
    }
    
    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
