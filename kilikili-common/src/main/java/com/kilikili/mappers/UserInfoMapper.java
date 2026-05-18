package com.kilikili.mappers;

import com.kilikili.entity.po.UserInfo;
import com.kilikili.entity.query.UserInfoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserInfoMapper {
    
    /**
     * 插入用户信息
     */
    Integer insert(UserInfo userInfo);
    
    /**
     * 批量插入用户信息
     */
    Integer insertBatch(@Param("list") List<UserInfo> list);
    
    /**
     * 根据用户ID删除用户
     */
    Integer deleteByUserId(@Param("userId") String userId);
    
    /**
     * 根据邮箱删除用户
     */
    Integer deleteByEmail(@Param("email") String email);
    
    /**
     * 根据昵称删除用户
     */
    Integer deleteByNickName(@Param("nickName") String nickName);
    
    /**
     * 批量删除用户
     */
    Integer deleteBatch(@Param("userIdList") List<String> userIdList);
    
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
    Integer updateByCondition(@Param("userInfo") UserInfo userInfo, @Param("query") UserInfoQuery query);
    
    /**
     * 根据用户ID查询用户
     */
    UserInfo selectByUserId(@Param("userId") String userId);
    
    /**
     * 根据邮箱查询用户
     */
    UserInfo selectByEmail(@Param("email") String email);
    
    /**
     * 根据昵称查询用户
     */
    UserInfo selectByNickName(@Param("nickName") String nickName);
    
    /**
     * 查询所有用户列表
     */
    List<UserInfo> selectList();
    
    /**
     * 根据条件查询用户列表
     */
    List<UserInfo> selectListByCondition(@Param("query") UserInfoQuery query);
    
    /**
     * 根据条件查询用户数量
     */
    Long selectCountByCondition(@Param("query") UserInfoQuery query);
}
