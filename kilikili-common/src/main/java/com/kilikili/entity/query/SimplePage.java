package com.kilikili.entity.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class SimplePage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 当前页码
    private Integer pageNo;
    
    // 每页大小
    private Integer pageSize;
    
    // 总记录数
    private Long totalCount;
    
    // 总页数
    private Integer totalPage;
    
    // 起始索引
    private Integer startIndex;
    
    public SimplePage() {
    }
    
    public SimplePage(Integer pageNo, Integer pageSize, Long totalCount) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.totalPage = (int) Math.ceil((double) totalCount / pageSize);
        this.startIndex = (pageNo - 1) * pageSize;
    }
}
