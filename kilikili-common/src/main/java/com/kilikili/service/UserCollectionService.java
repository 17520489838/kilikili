package com.kilikili.service;

import com.kilikili.entity.vo.PaginationResultVO;

import java.util.Map;

public interface UserCollectionService {
    PaginationResultVO<Map<String, Object>> loadUserCollection(String userId, Integer pageNo);
}
