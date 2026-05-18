package com.kilikili.web.controller;


import com.kilikili.component.RedisComponent;
import com.kilikili.entity.constants.Constants;
import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.exception.BusinessException;
import com.kilikili.service.UserInfoService;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;


// 用户信息
@RestController(value = "UserInfoController")
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;


    //验证码
    @RequestMapping("/checkCode")
    public ResponseVO checkCode(){
        ArithmeticCaptcha captcha= new ArithmeticCaptcha( 100,42);
        String code = captcha.text();
        String checkCodeKey=redisComponent.saveCheckCode(code);
        String checkCodeBase64 = captcha.toBase64();

        Map<String,String> result = new HashMap<>();
        result.put("checkCode", checkCodeBase64);
        result.put("checkCodeKey", checkCodeKey);
        return getSuccessResponseVO(result);
    }

    //注册
    @RequestMapping("/register")
    public ResponseVO register (@NotEmpty @Email @Size(max=150) String email,
                                @NotEmpty @Size(max=20) String nickName,
                                @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String registerPassword,
                                @NotEmpty String checkCodeKey,
                                @NotEmpty String checkCode){
        try {
            if(!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))){
                throw new BusinessException("图片验证码不正确");
            }
            userInfoService.register(email,nickName,registerPassword);
            return  getSuccessResponseVO(null);
        }finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }

    }

    //登录
    @RequestMapping("/login")
    public ResponseVO login (HttpServletResponse response,// 响应对象
                                @NotEmpty @Email @Size(max=150) String email,
                             @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password,
                             @NotEmpty String checkCodeKey,
                             @NotEmpty String checkCode){
        try {
            if(!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))){
                throw new BusinessException("图片验证码不正确");
            }
            String ip=getIpAddr();
            TokenUserInfoDto tokenUserInfoDto =userInfoService.login(email,password,ip);
            saveToken2Cookie(response,tokenUserInfoDto.getToken());
            //设置 粉丝数,关注数,硬币数
            return  getSuccessResponseVO(tokenUserInfoDto);
        }finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    //自动登录
    @RequestMapping("/autoLogin")
    public ResponseVO autoLogin(HttpServletResponse response) {
        // 从Cookie中获取token
        String token = getTokenFromCookie();
        if (token == null || token.isEmpty()) {
            throw new BusinessException("未登录或登录已过期");
        }
        
        // 从Redis中获取用户信息
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException("登录已过期，请重新登录");
        }
        
        // 检查token是否过期
        if (tokenUserInfoDto.getExpireTime() != null && System.currentTimeMillis() > tokenUserInfoDto.getExpireTime()) {
            throw new BusinessException("登录已过期，请重新登录");
        }
        
        // 刷新token有效期
        redisComponent.saveTokenUserInfo(tokenUserInfoDto);
        
        // 更新Cookie中的token
        saveToken2Cookie(response, tokenUserInfoDto.getToken());
        
        return getSuccessResponseVO(tokenUserInfoDto);
    }

    //退出登录
    @RequestMapping("/logout")
    public ResponseVO logout(HttpServletResponse response) {
        // 从Cookie中获取token
        String token = getTokenFromCookie();
        
        if (token != null && !token.isEmpty()) {
            // 删除Redis中的token
            redisComponent.deleteToken(token);
        }
        
        // 清除Cookie中的token
        clearTokenFromCookie(response);
        
        return getSuccessResponseVO(null);
    }
}
