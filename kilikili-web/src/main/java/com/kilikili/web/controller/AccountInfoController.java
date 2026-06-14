package com.kilikili.web.controller;

import com.kilikili.entity.query.UserInfoQuery;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.mappers.UserInfoMapper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("webAccountInfoController")
@RequestMapping("/account")
@Validated
public class AccountInfoController extends ABaseController {

    @Resource
    private UserInfoMapper userInfoMapper;

    @RequestMapping("/getUserCountInfo")
    public ResponseVO getUserCountInfo() {
        UserInfoQuery query = new UserInfoQuery();
        Long count = userInfoMapper.selectCountByCondition(query);
        return getSuccessResponseVO(count);
    }
}
