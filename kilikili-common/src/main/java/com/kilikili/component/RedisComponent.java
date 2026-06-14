package com.kilikili.component;


import com.kilikili.entity.constants.Constants;
import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.redis.RedisUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

@Component
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    public String saveCheckCode(String code) {
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, Constants.REDIS_KEY_EXPIRES_ONE_MIN*10 );
        return checkCodeKey;
    }

    public String getCheckCode(String checkCodeKey) {
        return (String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }
    public void cleanCheckCode(String checkCodeKey) {
        redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    public void saveTokenUserInfo(TokenUserInfoDto tokenUserInfoDto) {
        String token = UUID.randomUUID().toString();
        tokenUserInfoDto.setExpireTime(System.currentTimeMillis() + Constants.REDIS_KEY_EXPIRES_ONE_DAY*7);
        tokenUserInfoDto.setToken(token);
        redisUtils.setex(Constants.REDIS_KEY_Token_Web + "token:" + token, tokenUserInfoDto, Constants.REDIS_KEY_EXPIRES_ONE_DAY*7);

    }

    /**
     * 根据token获取用户信息
     */
    public TokenUserInfoDto getTokenUserInfo(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_Token_Web + "token:" + token);
    }

    /**
     * 删除web用户token
     */
    public void deleteToken(String token) {
        if (token != null && !token.isEmpty()) {
            redisUtils.delete(Constants.REDIS_KEY_Token_Web + "token:" + token);
        }
    }

    /**
     * 删除admin用户token
     */
    public void deleteAdminToken(String token) {
        if (token != null && !token.isEmpty()) {
            redisUtils.delete(Constants.REDIS_KEY_Token_Admin + "token:" + token);
        }
    }

    public String saveTokenUserInfo4admin(String account) {
        String token = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_Token_Admin+ "token:" + token, account, Constants.REDIS_KEY_EXPIRES_ONE_DAY*7);
        return token;

    }

    public String getTokenUserInfoAdmin(String token) {
      return (String) redisUtils.get(Constants.REDIS_KEY_Token_Admin+ "token:" + token);

    }
}
