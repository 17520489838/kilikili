package com.kilikili.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.io.Serializable;

// 用户信息
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenUserInfoDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private String nickName;
    private String avatar;
    private Long expireTime;// 过期时间
    private String token;

    private Integer fansCount;
    private Integer currentCoinCount;
    private Integer focusCount;
}
