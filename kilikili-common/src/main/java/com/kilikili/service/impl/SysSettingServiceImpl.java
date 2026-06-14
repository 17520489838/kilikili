package com.kilikili.service.impl;

import com.kilikili.entity.po.SysSetting;
import com.kilikili.entity.query.SysSettingQuery;
import com.kilikili.mappers.SysSettingMapper;
import com.kilikili.service.SysSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("sysSettingService")
public class SysSettingServiceImpl implements SysSettingService {

    @Resource
    private SysSettingMapper sysSettingMapper;

    @Override
    public Map<String, String> getSetting() {
        SysSettingQuery query = new SysSettingQuery();
        List<SysSetting> list = sysSettingMapper.selectListByCondition(query);
        Map<String, String> result = new HashMap<>();
        for (SysSetting setting : list) {
            result.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSetting(Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            SysSetting existing = sysSettingMapper.selectBySettingKey(entry.getKey());
            if (existing != null) {
                existing.setSettingValue(entry.getValue());
                existing.setUpdateTime(new Date());
                sysSettingMapper.updateBySettingKey(existing);
            } else {
                SysSetting setting = new SysSetting();
                setting.setSettingKey(entry.getKey());
                setting.setSettingValue(entry.getValue());
                setting.setCreateTime(new Date());
                setting.setUpdateTime(new Date());
                sysSettingMapper.insert(setting);
            }
        }
    }
}
