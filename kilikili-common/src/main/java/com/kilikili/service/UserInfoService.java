package com.kilikili.service;

import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.po.UserInfo;
import com.kilikili.entity.query.UserInfoQuery;
import com.kilikili.entity.vo.PaginationResultVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface UserInfoService {

    /**
     * 根据用户ID删除用户
     */
    Integer deleteByUserId(String userId);
    
    /**
     * 根据邮箱删除用户
     */
    Integer deleteByEmail(String email);
    
    /**
     * 根据昵称删除用户
     */
    Integer deleteByNickName(String nickName);
    
    /**
     * 批量删除用户
     */
    Integer batchDeleteByUserId(List<String> userIdList);
    
    /**
     * 根据用户ID更新用户信息
     */
    Integer updateByUserId(UserInfo userInfo);
    
    /**
     * 根据邮箱更新用户信息
     */
    Integer updateByEmail(UserInfo userInfo);
    
    /**
     * 根据昵称更新用户信息
     */
    Integer updateByNickName(UserInfo userInfo);
    
    /**
     * 根据条件更新用户信息
     */
    Integer updateByCondition(UserInfo userInfo, UserInfoQuery query);
    
    /**
     * 根据用户ID查询用户
     */
    UserInfo getUserInfoByUserId(String userId);
    
    /**
     * 根据邮箱查询用户
     */
    UserInfo getUserInfoByEmail(String email);
    
    /**
     * 根据昵称查询用户
     */
    UserInfo getUserInfoByNickName(String nickName);
    
    /**
     * 查询所有用户列表
     */
    List<UserInfo> getUserInfoList();
    
    /**
     * 根据条件查询用户列表
     */
    List<UserInfo> getUserInfoListByCondition(UserInfoQuery query);
    
    /**
     * 根据条件查询用户数量
     */
    Long getUserInfoCountByCondition(UserInfoQuery query);
    
    /**
     * 分页查询用户列表
     */
    PaginationResultVO<UserInfo> getUserInfoListByPage(UserInfoQuery query);

    // 注册
    void register(String email, String nickName, String registerPassword);

    // 登录
    TokenUserInfoDto login(String email, String password , String ip);
}
