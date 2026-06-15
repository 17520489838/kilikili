package com.kilikili.entity.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseParma implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 页码
    private Integer pageNo = 1;
    
    // 每页大小
    private Integer pageSize = 20;
    
    // 排序字段
    private String orderBy;
    
    // 排序方式 asc/desc
    private String orderDirection = "desc";
    
    // 起始索引（用于分页 OFFSET）
    public Integer getStartIndex() {
        if (pageNo == null || pageNo < 1) {
            return 0;
        }
        if (pageSize == null || pageSize < 1) {
            return 0;
        }
        return (pageNo - 1) * pageSize;
    }
}
