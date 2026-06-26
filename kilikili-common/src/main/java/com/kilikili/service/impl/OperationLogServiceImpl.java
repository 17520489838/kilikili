package com.kilikili.service.impl;

import com.kilikili.entity.po.OperationLog;
import com.kilikili.entity.query.OperationLogQuery;
import com.kilikili.entity.query.SimplePage;
import com.kilikili.entity.vo.PaginationResultVO;
import com.kilikili.mappers.OperationLogMapper;
import com.kilikili.service.OperationLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("operationLogService")
public class OperationLogServiceImpl implements OperationLogService {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Override
    public PaginationResultVO<OperationLog> loadLog(Integer pageNo, Integer pageSize,
                                                    String operModule, String operType, String operUserName) {
        OperationLogQuery query = new OperationLogQuery();
        query.setPageNo(pageNo != null ? pageNo : 1);
        query.setPageSize(pageSize != null ? pageSize : 20);
        query.setOperModule(operModule);
        query.setOperType(operType);
        query.setOperUserName(operUserName);
        query.setOrderBy("create_time");
        query.setOrderDirection("desc");

        Long count = operationLogMapper.selectCountByCondition(query);
        SimplePage page = new SimplePage(query.getPageNo(), query.getPageSize(), count);
        List<OperationLog> list = operationLogMapper.selectListByCondition(query);
        return new PaginationResultVO<>(page, list);
    }

    @Override
    public void saveLog(OperationLog log) {
        operationLogMapper.insert(log);
    }

    @Override
    public void cleanAll() {
        operationLogMapper.cleanAll();
    }
}
