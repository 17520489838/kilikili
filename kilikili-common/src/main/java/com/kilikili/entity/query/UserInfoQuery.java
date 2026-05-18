package com.kilikili.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserInfoQuery extends BaseParma {
    private static final long serialVersionUID = 1L;
    
    // 用户ID
    private String userId;
    
    // 用户ID列表（批量查询）
    private List<String> userIdList;
    
    // 昵称（模糊查询）
    private String nickName;
    
    // 邮箱
    private String email;
    
    // 邮箱列表（批量查询）
    private List<String> emailList;
    
    // 性别
    private Integer sex;
    
    // 生日
    private String birthday;
    
    // 学校
    private String school;
    
    // 个人简介
    private String personIntroduction;
    
    // 加入时间开始
    private Date joinTimeStart;
    
    // 加入时间结束
    private Date joinTimeEnd;
    
    // 最后登录时间开始
    private Date lastLoginTimeStart;
    
    // 最后登录时间结束
    private Date lastLoginTimeEnd;
    
    // 最后登录IP
    private String lastLoginIp;
    
    // 用户状态
    private Integer status;
    
    // 空间公告
    private String noticeInfo;
    
    // 硬币总数
    private Integer coinCount;
    
    // 当前硬币数
    private Integer currentCoinCount;
    
    // 主题
    private Integer theme;
    
    // 关键词搜索（搜索昵称、邮箱、学校等）
    private String keyword;
    
    // 起始索引（用于分页）
    private Integer startIndex;
    
    public Integer getStartIndex() {
        if (this.getPageNo() != null && this.getPageSize() != null) {
            return (this.getPageNo() - 1) * this.getPageSize();
        }
        return 0;
    }
}
