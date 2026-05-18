package com.kilikili.entity.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseParma implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 页码
    private Integer pageNo = 1;
    
    // 每页大小
    private Integer pageSize = 10;
    
    // 排序字段
    private String orderBy;
    
    // 排序方式 asc/desc
    private String orderDirection = "desc";
}
