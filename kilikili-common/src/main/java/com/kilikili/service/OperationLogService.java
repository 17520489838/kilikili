package com.kilikili.service;

import com.kilikili.entity.po.OperationLog;
import com.kilikili.entity.vo.PaginationResultVO;

public interface OperationLogService {

    PaginationResultVO<OperationLog> loadLog(Integer pageNo, Integer pageSize,
                                             String operModule, String operType, String operUserName);

    void saveLog(OperationLog log);

    void cleanAll();
}
