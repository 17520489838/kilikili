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
        // 设置默认值，防止 null 导致空指针异常
        this.pageNo = (pageNo == null || pageNo < 1) ? 1 : pageNo;
        this.pageSize = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        this.totalCount = (totalCount == null) ? 0L : totalCount;
        
        // 计算总页数和起始索引
        this.totalPage = (int) Math.ceil((double) this.totalCount / this.pageSize);
        this.startIndex = (this.pageNo - 1) * this.pageSize;
    }
}
