package com.kilikili.admin.controller;


import com.kilikili.component.RedisComponent;
import com.kilikili.config.Appconfig;
import com.kilikili.entity.constants.Constants;
import com.kilikili.entity.enums.ResponseCodeEnum;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.exception.BusinessException;
import com.kilikili.service.UserInfoService;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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

    @Resource
    private Appconfig appconfig;

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

    //登录
    @RequestMapping("/login")
    public ResponseVO login (HttpServletResponse response,// 响应对象
                                @NotEmpty  @Size(max=150) String account,
                             @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password,
                             @NotEmpty String checkCodeKey,
                             @NotEmpty String checkCode){
        try {
            if(!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))){
                throw new BusinessException("图片验证码不正确");

            }

            if (!account.equals(appconfig.getAdminAccount())|| !password.equals(appconfig.getAdminPassword())){
                throw new BusinessException(ResponseCodeEnum.USER_NOT_EXIST_OR_PASSWORD_ERROR);
            }
            String token = redisComponent.saveTokenUserInfo4admin(account);
            saveToken2Cookie(response,token);
            return  getSuccessResponseVO(account);
        }finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    //退出登录
    @RequestMapping("/logout")
    public ResponseVO logout(HttpServletResponse response) {
        // 从Cookie中获取token
        String token = getTokenFromCookie();
        
        if (token != null && !token.isEmpty()) {
            // 删除Redis中的admin token
            redisComponent.deleteAdminToken(token);
        }
        
        // 清除Cookie中的token
        clearTokenFromCookie(response);
        
        return getSuccessResponseVO(null);
    }
}