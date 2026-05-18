package com.kilikili.entity.vo;

import com.kilikili.entity.query.SimplePage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PaginationResultVO<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 分页信息
    private SimplePage pageInfo;
    
    // 数据列表
    private List<T> list;
    
    public PaginationResultVO() {
    }
    
    public PaginationResultVO(SimplePage pageInfo, List<T> list) {
        this.pageInfo = pageInfo;
        this.list = list;
    }
}
